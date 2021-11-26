package it.unimore.fum.iot.client;

import com.google.gson.Gson;
import it.unimore.fum.iot.utils.SenMLPack;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.*;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;


/**
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project coap-demo-smartobject
 * @created 20/10/2020 - 09:19
 */
public class CoapAutomaticClient {

	private static final String COAP_ENDPOINT = "coap://127.0.0.1:5683";

	private static final String RESOURCE_DISCOVERY_ENDPOINT = "/.well-known/core";

	private static String RT_TEMPERATURE_SENSOR = "it.unimore.device.sensor.temperature";
	private static String RT_CAPSULE_SENSOR = "it.unimore.device.sensor.capsule_presence";
	private static String RT_COFFEE_ACTUATOR = "it.unimore.device.actuator.coffee";

	private static String targetTemperatureSensorUri = null;
	private static String targetCapsulePresenceSensorUri = null;
	private static String targetCoffeeActuatorUri = null;

	private static Gson gson = new Gson();

	private static boolean validateTargetDevice(CoapClient coapClient){

		try{

			Request request = new Request(Code.GET);
			request.setURI(String.format("%s%s",
					COAP_ENDPOINT,
					RESOURCE_DISCOVERY_ENDPOINT));
			request.setConfirmable(true);

			CoapResponse coapResp = coapClient.advanced(request);

			if(coapResp != null) {
				if (coapResp.getOptions().getContentFormat() == MediaTypeRegistry.APPLICATION_LINK_FORMAT) {
					Set<WebLink> links = LinkFormat.parse(coapResp.getResponseText());
					links.forEach(link -> {
						String uri = link.getURI();

						link.getAttributes().getAttributeKeySet().forEach(attributeKey -> {
							String key = attributeKey;
							String value = link.getAttributes().getAttributeValues(attributeKey).get(0);

							if(key.equals("rt") && value.equals(RT_TEMPERATURE_SENSOR))
								targetTemperatureSensorUri = uri;
							else if(key.equals("rt") && value.equals(RT_CAPSULE_SENSOR))
								targetCapsulePresenceSensorUri = uri;
							else if(key.equals("rt") && value.equals(RT_COFFEE_ACTUATOR))
								targetCoffeeActuatorUri = uri;
						});
					});

					if(targetTemperatureSensorUri != null && targetCapsulePresenceSensorUri != null && targetCoffeeActuatorUri != null)
						return true;
					else
						return false;

				} else {
					return false;
				}
			}
			else
				return false;

		} catch (Exception e){
			return false;
		}
	}

	private static boolean isCoffeeCapsuleAvailable(CoapClient coapClient){

		try{

			Request request = new Request(Code.GET);
			request.setOptions(new OptionSet().setAccept(MediaTypeRegistry.APPLICATION_SENML_JSON));
			request.setURI(String.format("%s%s",
					COAP_ENDPOINT,
					targetCapsulePresenceSensorUri));
			request.setConfirmable(true);

			CoapResponse coapResp = coapClient.advanced(request);

			if(coapResp != null) {

				String payload = coapResp.getResponseText();
				SenMLPack senMLPack = gson.fromJson(payload, SenMLPack.class);
				return senMLPack.get(0).getVb();
			}
			else
				return false;

		} catch (Exception e){
			return false;
		}
	}

	private static boolean triggerCoffee(CoapClient coapClient){

		try{

			Request request = new Request(Code.POST);
			request.setURI(String.format("%s%s",
					COAP_ENDPOINT,
					targetCoffeeActuatorUri));
			request.setConfirmable(true);

			CoapResponse coapResp = coapClient.advanced(request);

			if(coapResp != null && coapResp.getCode().equals(CoAP.ResponseCode.CHANGED))
				return true;
			else
				return false;

		} catch (Exception e){
			return false;
		}
	}

	public static void main(String[] args) {

		//Initialize coapClient
		CoapClient coapClient = new CoapClient();
		boolean isDeviceValid = validateTargetDevice(coapClient);

		if(isDeviceValid){
			System.out.println("Valid Target Device Detected !");
			if(isCoffeeCapsuleAvailable(coapClient)){
				System.out.println("Capsule Available !");
				boolean coffeeResult = triggerCoffee(coapClient);
				if(coffeeResult)
					System.out.println("Drink your Coffee ! :)");
				else
					System.out.println("Error making Coffee ! Please try later ...");
			}
			else
				System.out.println("Capsule Not Available ! Stopping ...");
		}
		else
			System.out.println("Error: Invalid Device Detected !");

	}
}