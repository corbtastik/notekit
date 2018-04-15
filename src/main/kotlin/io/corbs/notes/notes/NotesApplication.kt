package io.corbs.notes.notes

import com.fasterxml.jackson.annotation.JsonIgnore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.HandleBeforeCreate
import org.springframework.data.rest.core.annotation.RepositoryEventHandler
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

val LOG: Logger = LoggerFactory.getLogger(NotesApplication::class.java)

@SpringBootApplication
class NotesApplication

fun main(args: Array<String>) {
    runApplication<NotesApplication>(*args)
}

@Configuration
@EnableWebSecurity
class WebSecurityConfiguration: WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
                .authorizeRequests().anyRequest()
                .fullyAuthenticated().and().httpBasic()
    }
}

@Entity
data class Note(
    @Id @GeneratedValue var id: Long? = null,
    var tag: String? = null,
    var text: String? = null,
    @JsonIgnore var user: String? = null)

@RepositoryRestResource
interface NotesRepository: JpaRepository<Note, Long> {
    fun findAllByUser(user: String): List<Note>
}

@Component
@RepositoryEventHandler(Note::class)
class AddUserToNote {

    @HandleBeforeCreate
    fun handleCreate(note: Note) {
        val username: String = SecurityContextHolder.getContext().authentication.name
        LOG.debug("Creating Note ${note} for user ${username}")
    }
}

@Component
class DataInitializer(@Autowired val repo: NotesRepository): ApplicationRunner {

    @Throws(Exception::class)
    override fun run(args: ApplicationArguments) {

        for(i in 0..99) {
            repo.save(Note(tag="testing", text="Happy Happy Joy Joy", user = "user"))
        }

        repo.findAll().forEach{
            println(it)
        }
    }

}

// #mindblown - you don't have to code such things with data-rest starter included
//@RestController
//class NotesController(val repo: NotesRepository) {
//    @GetMapping("/")
//    fun notes(principle: Principal): List<Note> {
//        LOG.debug("Fetching Notes for ${principle.name}")
//        val notes = repo.findAllByUser(principle.name)
//        // TODO old school if
//        // old school if/else blocks
//        // if(notes.isEmpty()) {
//        //    return listOf()
//        // } else {
//        //    return notes
//        // }
//        // TODO idiomatic Kotlin if
//        // this is more idiomatic to Kotlin
//        return if(notes.isEmpty()) {
//            listOf()
//        } else {
//            notes
//        }
//    }

//    @PostMapping("/")
//    fun notes(note: Note, principle: Principal): Note {
//        note.user = principle.name ?: "user"
//        return repo.save(note)
//    }
//}
