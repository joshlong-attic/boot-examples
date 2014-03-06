package demo;

import com.google.gwt.thirdparty.guava.common.base.Predicate;
import com.google.gwt.thirdparty.guava.common.collect.Collections2;
import com.google.gwt.thirdparty.guava.common.collect.ImmutableMap;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vaadin.spring.VaadinUI;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;


@ComponentScan
@EnableAutoConfiguration
public class Application extends SpringBootServletInitializer {

    private static Class<Application> entryPointClass = Application.class;

    public static void main(String[] args) {
        SpringApplication.run(entryPointClass, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(entryPointClass);
    }
}


@VaadinUI
class RootUI extends UI {

    @Autowired
    CustomerRepository customerRepository;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        getPage().setTitle("Root UI");

        Table table = new Table("Customer Table");

        table.addContainerProperty("firstName", String.class, null);
        table.addContainerProperty("lastName", String.class, null);
        table.addContainerProperty("id", Long.class, null);

        for (Customer c : this.customerRepository.findAll())
            table.addItem(new Object[]{c.getFirstName(), c.getLastName(), c.getId()}, c.getId());

        table.setSizeFull();
        table.setColumnHeader("firstName", "First Name");
        table.setColumnHeader("lastName", "First Name");
        setContent(table);
    }

}

@RestController
class CustomerRestController {

    @RequestMapping("/customers")
    Collection<Customer> customers() {
        return this.customerRepository.findAll();
    }

    @Autowired
    CustomerRepository customerRepository;
}


interface CustomerRepository extends JpaRepository<Customer, Long> {
}


@Entity
class Customer {
    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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
    private String firstName;
    private String lastName;

}
