package vip.testops.gungnir.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vip.testops.gungnir.commons.Response;
import vip.testops.gungnir.dao.PersonRepository;
import vip.testops.gungnir.entities.dto.Person;

@Service
public class PersonService {
    private PersonRepository personRepository;
    @Autowired
    public void setPersonRepository(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void savePerson(Person person, Response<?> response) {
        personRepository.save(person);
        response.commonSuccess();
    }
}
