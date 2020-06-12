package org.thekiddos.manager;

import org.thekiddos.manager.models.*;
import org.thekiddos.manager.repositories.Database;

public abstract class AddEmployeeTransaction implements Transaction {
    private Long id;
    private String name;

    public AddEmployeeTransaction( Long id, String name ) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void execute() {
        PaymentClassification paymentClassification = getPaymentClassification();
        PaymentSchedule paymentSchedule = getPaymentSchedule();
        PaymentMethod paymentMethod = new HoldMethod();

        Employee emp = new Employee( id, name );
        emp.setPaymentClassification( paymentClassification );
        emp.setPaymentSchedule( paymentSchedule );
        emp.setPaymentMethod( paymentMethod );

        Database.addEmployee( emp );
    }

    protected abstract PaymentClassification getPaymentClassification();
    protected abstract PaymentSchedule getPaymentSchedule();
}
