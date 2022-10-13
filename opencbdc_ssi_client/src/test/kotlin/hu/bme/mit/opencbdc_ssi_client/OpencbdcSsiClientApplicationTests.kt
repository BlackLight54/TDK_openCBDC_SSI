package hu.bme.mit.opencbdc_ssi_client

import com.sun.jdi.Bootstrap
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.event.annotation.BeforeTestClass

@SpringBootTest(properties = arrayOf("spring.shell.interactive.enabled=false"))
class OpencbdcSsiClientApplicationTests() {

    @BeforeTestClass
    fun init(){

    }
    @Test
    fun contextLoads() {
    }

}
