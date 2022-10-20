package hu.bme.mit.opencbdc_ssi_client.credientials

import lombok.Data
import org.hyperledger.aries.pojo.AttributeGroupName
import org.hyperledger.aries.pojo.AttributeName

@Data
@AttributeGroupName("referent")
class CitizenCred(@AttributeName("age") val age: Int, @AttributeName("name") val name: String) {
    @AttributeName("citizenship")val citizenship : String = "Hungarian"
}