package hu.bme.mit.opencbdc_ssi_client

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class Endpoints {

        @GetMapping("/onboard")
        public fun index(): String {
            return "onboard Alice"
        }

}