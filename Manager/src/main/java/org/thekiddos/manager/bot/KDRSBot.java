package org.thekiddos.manager.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.thekiddos.manager.Util;
import org.thekiddos.manager.models.Customer;
import org.thekiddos.manager.models.Item;
import org.thekiddos.manager.models.TelegramUser;
import org.thekiddos.manager.repositories.Database;
import org.thekiddos.manager.services.EmailServiceImpl;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class KDRSBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger( KDRSBot.class );
    private TelegramUser currentTelegramUser;
    private SendMessage response;
    private SendPhoto responseWithImage;

    private EmailServiceImpl emailService;

    @Autowired
    public void setEmailService( EmailServiceImpl emailService ) {
        this.emailService = emailService;
    }

    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived( Update update ) {
        if ( !update.hasMessage() ) {
            return;
        }

        Message message = update.getMessage();
        String messageText = message.getText();
        Long chatId = message.getChatId();

        response = new SendMessage();
        response.setChatId( chatId );

        logger.info( String.valueOf( message.getFrom().getId() ) );
        currentTelegramUser = Database.getTelegramUser( message.getFrom().getId() );

        processRequestMessage( messageText );

        try {
            if ( response == null )
                execute( responseWithImage ) ;
            else
                execute( response );
            logger.info( "Sent message \"{}\" to {}", messageText, chatId );
        } catch (TelegramApiException e) {
            logger.error( "Failed to send message \"{}\" to {} due to error: {}", messageText, chatId, e.getMessage() );
        }
    }

    private void processRequestMessage( String messageText ) {
        // Commands must come before checking for last commands.
        if ( messageText.equals( Commands.EMAIL ) ) {
            sendInstructions( "Please enter the same email you used to create account at our website.", Commands.EMAIL );
        }
        else if ( messageText.equals( Commands.VERIFY ) ) {
            processVerificationRequest();
        }
        else if ( messageText.startsWith( Commands.ITEMS ) ) {
            sendItemsDetails( messageText );
        }
        else if ( messageText.equals( Commands.RECOMMEND ) ) {
            recommendItemsForUser();
        }
        else if ( currentTelegramUser.getLastCommand().equals( Commands.EMAIL ) ) {
            setEmail( messageText );
        }
        else if ( currentTelegramUser.getLastCommand().equals( Commands.VERIFY ) ) {
            try{
                int code = Integer.parseInt( messageText );
                verifyCode( code );
            }
            catch ( NumberFormatException e ) {
                sendInstructions( "That's not a number dumb ass" );
            }
        }
        else {
            sendInstructions( "Go to HELL!" );
        }
    }

    private void sendInstructions( String instructions, String lastCommand ) {
        sendInstructions( instructions );
        currentTelegramUser.setLastCommand( lastCommand );
        Database.updateTelegramUser( currentTelegramUser );
    }

    private void sendInstructions( String instructions ) {
        response.setText( instructions );
    }

    private void processVerificationRequest() {
        String email = currentTelegramUser.getEmail();

        if ( !emailExists( email ) ) {
            sendInstructions( "Please enter your email first using the /email command", Commands.NOTHING );
            return;
        }

        if ( currentTelegramUser.isVerified() ) {
            sendInstructions( "Your account is already verified.", Commands.NOTHING );
            return;
        }

        int code = generateVerificationCode();
        currentTelegramUser.setVerificationCode( code ); // the send instruction will update the user in the database
        emailService.sendEmail( email, "Verify your email", "Please enter the following code in telegram\n" + code );

        response.setText( "Please enter the code sent to your email" );
        sendInstructions( "Please enter the code sent to your email", Commands.VERIFY );
    }

    private boolean emailExists( String email ) {
        return email == null || email.length() == 0;
    }

    private int generateVerificationCode() {
        return ThreadLocalRandom.current().nextInt( 12345, 99999 );
    }

    private void sendItemsDetails( String messageText ) {
        Long itemId = extractItemId( messageText );
        if ( itemId.equals( Util.INVALID_ID ) ) {
            sendAllItems();
        }
        else {
            sendItemDetails( itemId );
        }
        currentTelegramUser.setLastCommand( Commands.ITEMS );
        Database.updateTelegramUser( currentTelegramUser );
    }

    private Long extractItemId( String messageText ) {
        try {
            return Long.parseLong( messageText.split( "\\s" )[ 1 ] );
        }
        catch ( Exception e ) {
            return Util.INVALID_ID;
        }
    }

    private void sendAllItems() {
        Set<Item> items = Database.getItems();
        response.setText( getItemsDetails( items ) );
    }

    private String getItemsDetails( Set<Item> items ) {
        StringBuilder itemDetails = new StringBuilder();
        items.forEach( item -> itemDetails.append( item.getId() ).append( "." ).append( item.getName() )
                .append( ", Price: " ).append( item.getPrice() ).append( "\n\n" ) );
        return itemDetails.toString();
    }

    private void sendItemDetails( Long itemId ) {
        Item item = Database.getItemById( itemId );

        SendPhoto responsePhoto = new SendPhoto();
        responsePhoto.setChatId( response.getChatId() );
        responsePhoto.setPhoto( new File( item.getImagePath() ) ); // TODO test it
        responsePhoto.setCaption( item.getName() + "\nPrice: " + item.getPrice() + "\n" + item.getDescription() );

        response = null;
    }

    private void recommendItemsForUser() {
        Set<Item> recommendedItems = Database.getRecommendationsFor( currentTelegramUser.getEmail() );
        response.setText( getItemsDetails( recommendedItems ) );

        currentTelegramUser.setLastCommand( Commands.RECOMMEND );
        Database.updateTelegramUser( currentTelegramUser );
    }

    private void setEmail( String email ) {
        if ( !Util.validateEmail( email ) ) {
            sendInstructions( "Enter your email in the correct format." );
            return;
        }

        Customer customer = Database.getCustomerByEmail( email );
        if ( customer == null ) {
            sendInstructions( "Please register first using our website." );
            return;
        }

        currentTelegramUser.setEmail( email );
        currentTelegramUser.setVerified( false );
        sendInstructions( "Your email was successfully saved. please verify it with the /verify command", Commands.NOTHING ); // updates the user
    }

    private void verifyCode( int code ) {
        if ( code != currentTelegramUser.getVerificationCode() ) {
            clearOldVerificationCode();
            sendInstructions( "Code doesn't match, try again", Commands.NOTHING );
            return;
        }

        currentTelegramUser.setVerified( true );
        sendInstructions( "Your account was verified!.\n your account is now linked.", Commands.NOTHING );
    }

    private void clearOldVerificationCode() {
        currentTelegramUser.setVerificationCode( null );
    }

    @PostConstruct
    public void start() {
        logger.info("Starting Bot\n");
    }
}
