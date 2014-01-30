package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
public class CustomerRestController {

    @RequestMapping("/customers")
    Collection<Customer> customers(){
        return this.customerRepository.findAll() ;
    }

    @Autowired CustomerRepository customerRepository ;
}
