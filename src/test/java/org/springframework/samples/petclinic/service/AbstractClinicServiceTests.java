/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p> Base class for {@link ClinicService} integration tests. </p> <p> Subclasses should specify Spring context
 * configuration using {@link ContextConfiguration @ContextConfiguration} annotation </p> <p>
 * AbstractclinicServiceTests and its subclasses benefit from the following services provided by the Spring
 * TestContext Framework: </p> <ul> <li><strong>Spring IoC container caching</strong> which spares us unnecessary set up
 * time between test execution.</li> <li><strong>Dependency Injection</strong> of test fixture instances, meaning that
 * we don't need to perform application context lookups. See the use of {@link Autowired @Autowired} on the <code>{@link
 * AbstractclinicServiceTests#clinicService clinicService}</code> instance variable, which uses autowiring <em>by
 * type</em>. <li><strong>Transaction management</strong>, meaning each test method is executed in its own transaction,
 * which is automatically rolled back by default. Thus, even if tests insert or otherwise change database state, there
 * is no need for a teardown or cleanup script. <li> An {@link org.springframework.context.ApplicationContext
 * ApplicationContext} is also inherited and can be used for explicit bean lookup if necessary. </li> </ul>
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public abstract class AbstractClinicServiceTests {

    @Autowired
    protected ClinicService clinicService;

    @Test
    public void shouldFindOwners() {
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("Davis");
        assertThat(owners.size()).isEqualTo(2);
        owners = this.clinicService.findOwnerByLastName("Daviss");
        assertThat(owners.size()).isEqualTo(0);
    }

    @Test
    public void shouldFindSingleOwner() {
        Owner owner1 = this.clinicService.findOwnerById(1);
        assertThat(owner1.getLastName()).startsWith("Franklin");

        Owner owner10 = this.clinicService.findOwnerById(10);
        assertThat(owner10.getFirstName()).isEqualTo("Carlos");
        assertThat(owner1.getPets().size()).isEqualTo(1);
    }

    @Test
    @Transactional
    public void shouldInsertOwner() {
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("Schultz");
        int found = owners.size();
        Owner owner = new Owner();
        owner.setFirstName("Sam");
        owner.setLastName("Schultz");
        owner.setAddress("4, Evans Street");
        owner.setCity("Wollongong");
        owner.setTelephone("4444444444");
        this.clinicService.saveOwner(owner);
        assertThat(owner.getId().longValue()).isNotEqualTo(0);

        owners = this.clinicService.findOwnerByLastName("Schultz");
        assertThat(owners.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    public void shouldUpdateOwner()  {
        Owner o1 = this.clinicService.findOwnerById(1);
        String old = o1.getLastName();
        o1.setLastName(old + "X");
        this.clinicService.saveOwner(o1);
        o1 = this.clinicService.findOwnerById(1);

        assertThat(o1.getLastName()).isEqualTo(old + "X");
    }

	@Test
	public void shouldFindPetWithCorrectId() {
	    Collection<PetType> types = this.clinicService.findPetTypes();
	    
	    Pet pet7 = this.clinicService.findPetById(7);
	    assertThat(pet7.getName()).startsWith("Samantha");
	    assertThat(EntityUtils.getById(types, PetType.class, 1).getId()).isEqualTo(pet7.getType().getId());
	    assertThat(pet7.getOwner().getFirstName()).isEqualTo("Jean");
	    
	    Pet pet6 = this.clinicService.findPetById(6);
	    assertThat(pet6.getName()).isEqualTo("George");
	    
	    assertThat(EntityUtils.getById(types, PetType.class, 4).getId()).isEqualTo(pet6.getType().getId());
	    assertThat(pet6.getOwner().getFirstName()).isEqualTo("Peter");
	}

	@Test
	public void shouldFindAllPetTypes() {
	    Collection<PetType> petTypes = this.clinicService.findPetTypes();
	
	    PetType petType1 = EntityUtils.getById(petTypes, PetType.class, 1);
	    assertThat(petType1.getName()).isEqualTo("cat");
	    PetType petType4 = EntityUtils.getById(petTypes, PetType.class, 4);
	    assertThat(petType4.getName()).isEqualTo("snake");
	}

	@Test
	@Transactional
	public void shouldInsertPetIntoDatabaseAndGenerateId() {
	    Owner owner6 = this.clinicService.findOwnerById(6);
	    int found = owner6.getPets().size();
	    Pet pet = new Pet();
	    pet.setName("bowser");
	    Collection<PetType> types = this.clinicService.findPetTypes();
	    pet.setType(EntityUtils.getById(types, PetType.class, 2));
	    pet.setBirthDate(new DateTime());
	    owner6.addPet(pet);
	    assertThat(owner6.getPets().size()).isEqualTo(found + 1);
	    // both storePet and storeOwner are necessary to cover all ORM tools
	    this.clinicService.savePet(pet);
	    this.clinicService.saveOwner(owner6);
	    owner6 = this.clinicService.findOwnerById(6);
	    assertThat(owner6.getPets().size()).isEqualTo(found + 1);
	    assertThat(pet.getId()).isNotNull();
	}

	@Test
	@Transactional
	public void sholdUpdatePet() throws Exception {
	    Pet pet7 = this.clinicService.findPetById(7);
	    String old = pet7.getName();
	    pet7.setName(old + "X");
	    this.clinicService.savePet(pet7);
	    pet7 = this.clinicService.findPetById(7);
	    assertThat(pet7.getName()).isEqualTo(old + "X");
	}

	@Test
	public void shouldFindVets() {
	    Collection<Vet> vets = this.clinicService.findVets();
	
	    Vet v1 = EntityUtils.getById(vets, Vet.class, 2);
	    assertThat(v1.getLastName()).isEqualTo("Leary");
	    assertThat(v1.getNrOfSpecialties()).isEqualTo(1);
	    assertThat(v1.getSpecialties().get(0).getName()).isEqualTo("radiology");
	    Vet v2 = EntityUtils.getById(vets, Vet.class, 3);
	    assertThat(v2.getLastName()).isEqualTo("Douglas");
	    assertThat(v2.getNrOfSpecialties()).isEqualTo(2);
	    assertThat(v2.getSpecialties().get(0).getName()).isEqualTo("dentistry");
	    assertThat(v2.getSpecialties().get(1).getName()).isEqualTo("surgery");
	}

	@Test
	@Transactional
	public void shouldAddNewVisitForPet() {
	    Pet pet7 = this.clinicService.findPetById(7);
	    int found = pet7.getVisits().size();
	    Visit visit = new Visit();
	    pet7.addVisit(visit);
	    visit.setDescription("test");
	    // both storeVisit and storePet are necessary to cover all ORM tools
	    this.clinicService.saveVisit(visit);
	    this.clinicService.savePet(pet7);
	    pet7 = this.clinicService.findPetById(7);
	    assertThat(pet7.getVisits().size()).isEqualTo(found + 1);
	    assertThat(visit.getId()).isNotNull();
	}


}