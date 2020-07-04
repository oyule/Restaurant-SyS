package org.thekiddos.manager.repositories;

import org.springframework.context.ApplicationContext;
import org.thekiddos.manager.models.Customer;
import org.thekiddos.manager.models.Item;
import org.thekiddos.manager.models.Reservation;
import org.thekiddos.manager.models.SittingTable;
import org.thekiddos.manager.payroll.models.Employee;

import java.time.LocalDate;
import java.util.*;

public class Database {
    private static final Map<Long, Employee> employees = new HashMap<>();
    private static final Map<Long, Customer> customers = new HashMap<>();
    private static final Map<Long, SittingTable> tables = new HashMap<>();
    private static final Map<Long, Item> items = new HashMap<>();
    private static final Map<Long, List<Reservation>> reservations = new HashMap<>();

    private static ApplicationContext applicationContext;
    private static TableRepository tableRepository;
    // TODO rename getAll methods to include id in the name
    // TODO use the Optional later

    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    public static void setUpDatabase( ApplicationContext applicationContext ) {
        Database.applicationContext = applicationContext;
        tableRepository = getBean( TableRepository.class );
    }

    public static Employee getEmployeeById( Long employeeId ) {
        return employees.get( employeeId );
    }

    public static void addEmployee( Employee employee ) {
        employees.put( employee.getId(), employee );
    }

    public static void removeEmployeeById( Long empId ) {
        employees.remove( empId );
    }

    public static List<Employee> getEmployees() {
        return new ArrayList<>( employees.values() );
    }

    public static void addCustomer( Customer customer ) {
        customers.put( customer.getId(), customer );
    }

    public static Customer getCustomerById( Long customerId ) {
        return customers.get( customerId );
    }

    public static void removeCustomerById( Long customerId ) {
        customers.remove( customerId );
    }

    public static void addTable( SittingTable table ) {
        tableRepository.save( table );
    }

    public static SittingTable getTableById( Long tableId ) {
        Optional<SittingTable> optionalSittingTable = tableRepository.findById( tableId );
        return optionalSittingTable.orElse( null );
    }

    public static void removeTableById( Long tableId ) {
        tableRepository.deleteById( tableId );
    }

    public static void addReservation( Reservation reservation ) {
        List<Reservation> tableReservations = reservations.get( reservation.getTableId() );
        if ( tableReservations == null )
            tableReservations = new ArrayList<>();

        tableReservations.add( reservation );

        reservations.put( reservation.getTableId(), tableReservations );
    }

    public static List<Reservation> getReservationsByTableId( Long tableId ) {
        return reservations.computeIfAbsent( tableId, k -> new ArrayList<>() );
    }

    public static List<Reservation> getReservationsByCustomerId( Long customerId ) {
        List<Reservation> customerReservations = new ArrayList<>();
        for ( List<Reservation> tableReservations : reservations.values() )
            for ( Reservation reservation : tableReservations )
                if ( reservation.getCustomerId().equals( customerId ) )
                    customerReservations.add( reservation );
        return customerReservations;
    }

    public static void deleteReservation( Long tableId, LocalDate reservationDate ) {
        List<Reservation> tableReservation = Database.getReservationsByTableId( tableId );
        for ( int i = 0; i < tableReservation.size(); ++i )
            if ( tableReservation.get( i ).getDate().equals( reservationDate ) ) {
                tableReservation.remove( i );
                return;
            }
    }

    public static Set<Long> getItemsId() {
        return new HashSet<>( items.keySet() );
    }

    public static Item getItemById( Long itemId ) {
        return items.get( itemId );
    }

    public static void addItem( Item item ) {
        items.put( item.getId(), item );
    }

    public static void removeItemById( Long itemId ) {
        items.remove( itemId );
    }

    public static Reservation getCurrentReservationByTableId( Long tableId ) {
        List<Reservation> tableReservation = Database.getReservationsByTableId( tableId );
        LocalDate now = LocalDate.now();
        for ( Reservation reservation : tableReservation ) {
            if ( reservation.getDate().equals( now ) )
                return reservation;
        }
        return null;
    }

    public static void init() {
        employees.clear();
        customers.clear();
        tables.clear();
        items.clear();
        reservations.clear();
    }

    public static Set<Long> getFreeTablesOn( LocalDate date ) {
        Set<Long> freeTablesOnDate = getTablesId();
        Set<Long> reservedTables = new HashSet<>();
        for ( List<Reservation> tableReservations : reservations.values() ) {
            tableReservations.stream().filter( reservation -> reservation.getDate().equals( date ) )
                    .forEach( reservation -> reservedTables.add( reservation.getTableId() ) );
        }

        freeTablesOnDate.removeAll( reservedTables );
        return freeTablesOnDate;
    }

    public static Set<Long> getTablesId() {
        return new HashSet<>( tables.keySet() );
    }

    public static Set<Long> getCustomersId() {
        return new HashSet<>( customers.keySet() );
    }

    public static List<Reservation> getCurrentReservations() {
        List<Reservation> currentReservations = new ArrayList<>();
        for ( Long tableId : getTablesId() ) {
            Reservation reservation = getCurrentReservationByTableId( tableId );
            if ( reservation != null )
                currentReservations.add( reservation );
        }
        return currentReservations;
    }

    public static Set<Item> getItems() {
        return new HashSet<>( items.values() );
    }

    public static Set<Long> getEmployeesId() {
        return new HashSet<>( employees.keySet() );
    }

    // TODO protect against nulls in all Transaction/Models
}
