package demo;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.vaadin.spring.VaadinUI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;

@EnableAutoConfiguration
@ComponentScan
public class Application {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}

@VaadinUI
@Theme("sample")
class RootUI extends UI {

    @Autowired
    CustomerRepository customerRepository;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        getPage().setTitle("Root UI");
        // setContent(new Label("Hello! I'm the root UI!"));
        setContent(this.table());
    }

    Table table() {
        Table table = new Table("This is my Table");

        table.addContainerProperty("First Name", String.class, null);
        table.addContainerProperty("Last Name", String.class, null);
        table.addContainerProperty("ID", Long.class, null);

        Collection<Customer> customerCollection = this.customerRepository.findAll();

        for (Customer c : customerCollection)
            table.addItem(new Object[]{c.getFirstName(), c.getLastName(), c.getId()}, c.getId());

        return table;
    }
}

@VaadinUI(path = "/anotherUI")
class AnotherUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        getPage().setTitle("Another UI");
        setContent(new Label("Hello! I'm a different UI mapped at a different URL!"));
    }
}

interface CustomerRepository extends JpaRepository<Customer, Long> {
}

@Entity
class Customer {
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Customer() {
    }

    public Customer(long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Id
    @GeneratedValue
    private long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

}