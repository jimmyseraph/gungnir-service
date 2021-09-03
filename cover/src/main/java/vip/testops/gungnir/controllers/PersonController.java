package vip.testops.gungnir.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.testops.gungnir.commons.Response;
import vip.testops.gungnir.entities.dto.Person;
import vip.testops.gungnir.services.PersonService;

@RestController
@RequestMapping("/person")
public class PersonController {
    private PersonService personService;

    @Autowired
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping("/add")
    public Response<?> addPerson(@RequestBody Person person) {
        Response<?> response = new Response<>();
        personService.savePerson(person, response);
        return response;
    }
}
