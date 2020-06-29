package org.thekiddos.manager.transactions;

import org.thekiddos.manager.models.Reservation;
import org.thekiddos.manager.repositories.Database;

public class ActivateReservationTransaction implements Transaction {
    private final Reservation reservation;

    public ActivateReservationTransaction( Long tableId ) {
        reservation = Database.getCurrentReservationByTableId( tableId );
        if ( reservation == null ) // TODO Check time too bruh
            throw new IllegalArgumentException( "No current reservations for the selected table" );
    }

    @Override
    public void execute() {
        reservation.activate();
    }
}