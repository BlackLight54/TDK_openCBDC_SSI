package hu.bme.mit.opencbdc_ssi_client.credientials

import lombok.Data
import org.hyperledger.aries.pojo.AttributeGroupName
import org.hyperledger.aries.pojo.AttributeName

@Data
@AttributeGroupName("CB referent")
class CBCred(@AttributeName("name") val name: String, @AttributeName("address")val address : String) {

}