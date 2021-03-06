package br.com.elotech.person.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import br.com.elotech.person.models.Contact;
import br.com.elotech.person.models.Person;
import br.com.elotech.person.repositories.PersonRepository;

@ExtendWith(MockitoExtension.class)
public class PersonServiceUnitTest {
  
  @InjectMocks
  PersonService service;
  @Mock
  PersonRepository repository;
  
  Person person1;
  Person person2;

  @BeforeEach
  void init()
  {
    List<Contact> contacts1 = Arrays.asList(
      new Contact(1l, "Carl Krank", "85986469382", "carlkrank@mail.com", null)
    );

    List<Contact> contacts2 = Arrays.asList(
      new Contact(2l, "Bob Odenkirk", "85987527584", "bobodenkirk@mail.com", null)
    );

    person1 = new Person(1l, "Bob Odenkirk", "81644673088", LocalDate.of(1998, 1, 23), contacts1);
    person2 = new Person(2l, "Carl Krank", "50023712058", LocalDate.of(1992, 4, 1), contacts2);
  }

  @Test
  void testFindAll()
  {
    when(
      repository.findAll("446", null)
    ).thenReturn(
      new PageImpl<>(Arrays.asList(person1))
    );

    when(
      repository.findAll("1992-04-01", null)
    ).thenReturn(
      new PageImpl<>(Arrays.asList(person2))
    );

    Page<Person> response = service.findAll("446", null);
    verify(repository).findAll("446", null);
    
    assertThat(response.getContent().size()).isEqualTo(1);
    assertThat(response.getContent().get(0).getName()).isEqualTo(person1.getName());

    response = service.findAll("1992-04-01", null);
    verify(repository).findAll("1992-04-01", null);

    assertThat(response.getContent().size()).isEqualTo(1);
    assertThat(response.getContent().get(0).getName()).isEqualTo(person2.getName());
  }

  @Test
  void testFindById()
  {
    when(
      repository.findById(1l)
    ).thenReturn(
      Optional.of(person1)
    );

    when(
      repository.findById(2l)
    ).thenReturn(
      Optional.of(person2)
    );

    Optional<Person> response = service.findById(1l);
    verify(repository).findById(1l);
    assertThat(response.get().getName()).isEqualTo(person1.getName());
    
    response = service.findById(2l);
    verify(repository).findById(2l);
    assertThat(response.get().getName()).isEqualTo(person2.getName());
  }

  @Test
  void testIsValid()
  {
    assertThat(service.isValid(person1)).isTrue();

    List<Contact> contacts2 = Arrays.asList(
      new Contact(null, "Bob Odenkirk", "85987527584", "bobodenkirk@mail.com", null),
      new Contact(null, "Carl Krank", "85986469382", "carlkrank@mail.com", null)
    );

    assertThat(service.isValid(
      new Person(null, null, "97694818077", LocalDate.of(1997, 4, 1), contacts2))
    ).isFalse();
    assertThat(service.isValid(
      new Person(null, "John Kirkman", null, LocalDate.of(1997, 4, 1), contacts2))
    ).isFalse();
    assertThat(service.isValid(
      new Person(null, "John Kirkman", "97694818071", LocalDate.of(1997, 4, 1), contacts2))
    ).isFalse();
    assertThat(service.isValid(
      new Person(null, "John Kirkman", "97694818077", null, contacts2))
    ).isFalse();
    assertThat(service.isValid(
      new Person(null, "John Kirkman", "97694818077", LocalDate.now().plusDays(1), contacts2))
    ).isFalse();
    assertThat(service.isValid(
      new Person(null, "John Kirkman", "97694818077", LocalDate.of(1997, 4, 1), null))
    ).isFalse();
    assertThat(service.isValid(
      new Person(null, "John Kirkman", "97694818077", LocalDate.of(1997, 4, 1), new ArrayList<Contact>()))
    ).isFalse();

    person1.setContacts(Arrays.asList(new Contact(null, null, "85987527584", "bobodenkirk@mail.com", null)));
    assertThat(service.isValid(person1)).isFalse();
    person1.setContacts(Arrays.asList(new Contact(null, "Bob Odenkirk", null, "bobodenkirk@mail.com", null)));
    assertThat(service.isValid(person1)).isFalse();
    person1.setContacts(Arrays.asList(new Contact(null, "Bob Odenkirk", "85987527584", null, null)));
    assertThat(service.isValid(person1)).isFalse();
    person1.setContacts(Arrays.asList(new Contact(null, "Bob Odenkirk", "85987527584", "invalidEmail", null)));
    assertThat(service.isValid(person1)).isFalse();
  }

  @Test
  void testStore()
  {
    when(repository.save(person1)).thenReturn(person1);
    service.store(person1);
    verify(repository).save(person1);
    assertThat(person1.getId()).isEqualTo(null);
    
    person1.getContacts().forEach(contact -> {
      assertThat(contact.getId()).isEqualTo(null);
      assertThat(contact.getPerson()).isEqualTo(person1);
    });
  }

  @Test
  void testUpdate()
  {
    when(repository.findById(1l)).thenReturn(Optional.of(person1));
    person1.setId(null);
    service.update(1l, person1);
    verify(repository).findById(1l);
    verify(repository).save(person1);
    assertThat(person1.getId()).isEqualTo(1l);
    person1.getContacts().forEach(contact -> {
      assertThat(contact.getId()).isEqualTo(null);
      assertThat(contact.getPerson()).isEqualTo(person1);
    });

    when(repository.findById(2l)).thenReturn(Optional.ofNullable(null));

    assertThatThrownBy(() -> service.update(2l, person1))
    .isInstanceOf(RuntimeException.class)
    .hasMessage("PersonNotFound");
  }

  @Test
  void delete()
  {
    when(repository.findById(1l)).thenReturn(Optional.of(person1));
    service.delete(1l);
    verify(repository).findById(1l);
    verify(repository).deleteById(1l);
    
    when(repository.findById(1l)).thenReturn(Optional.ofNullable(null));
    assertThatThrownBy(() -> service.delete(1l))
    .isInstanceOf(RuntimeException.class)
    .hasMessage("PersonNotFound");
  }
}
