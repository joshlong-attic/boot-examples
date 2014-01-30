package demo;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.VaadinUI;

@VaadinUI
public class RootUI extends UI {

    @Autowired
    CustomerRepository customerRepository;

    @Override
    protected void init(VaadinRequest vaadinRequest) {

        getPage().setTitle("Root UI");

        // setContent(new Label("Hello! I'm the root UI!"));

        BeanItemContainer<Customer> customerBeanItemContainer
                = new BeanItemContainer<>(Customer.class, this.customerRepository.findAll());
        Table table = new Table("Customer Table");
        table.setContainerDataSource(customerBeanItemContainer);
        table.setVisibleColumns("id", "firstName","lastName");
        table.setSizeFull();
        setContent(table);
    }

}
