package it.unimore.fum.iot.resource;

import com.google.gson.Gson;
import it.unimore.fum.iot.model.CoffeeHistoryDescriptor;
import it.unimore.fum.iot.request.MakeCoffeeRequestDescriptor;
import it.unimore.fum.iot.utils.CoreInterfaces;
import it.unimore.fum.iot.utils.SenMLPack;
import it.unimore.fum.iot.utils.SenMLRecord;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.Optional;

/**
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project coap-playground
 * @created 20/10/2020 - 21:54
 */
public class CoffeeActuatorResource extends CoapResource {

	private static final String OBJECT_TITLE = "CoffeeActuator";
	private Gson gson;
	private CoffeeHistoryDescriptor coffeeHistoryDescriptor;
	private String deviceId = null;
	private static final Number SENSOR_VERSION = 0.1;
	private String UNIT = "count";

	public CoffeeActuatorResource(String name, String deviceId) {
		super(name);
		this.deviceId = deviceId;
		init();
	}

	private void init(){

        this.gson = new Gson();
		this.coffeeHistoryDescriptor = new CoffeeHistoryDescriptor();

		setObservable(true);
		setObserveType(CoAP.Type.CON);

		getAttributes().setTitle(OBJECT_TITLE);
		getAttributes().addAttribute("rt", "it.unimore.device.actuator.coffee");
		getAttributes().addAttribute("if", CoreInterfaces.CORE_A.getValue());
		getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
		getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));
	}

	private Optional<String> getJsonSenmlResponse(){

		try{

			SenMLPack senMLPack = new SenMLPack();

			SenMLRecord senMLRecord = new SenMLRecord();
			senMLRecord.setBn(this.deviceId);
			senMLRecord.setBver(SENSOR_VERSION);
			senMLRecord.setBu(UNIT);
			senMLRecord.setT(System.currentTimeMillis());

			SenMLRecord shortCoffeeRecord = new SenMLRecord();
			shortCoffeeRecord.setN("short_coffee");
			shortCoffeeRecord.setV(this.coffeeHistoryDescriptor.getShortCount());

			SenMLRecord mediumCoffeeRecord = new SenMLRecord();
			mediumCoffeeRecord.setN("medium_coffee");
			mediumCoffeeRecord.setV(this.coffeeHistoryDescriptor.getMediumCount());

			SenMLRecord longCoffeeRecord = new SenMLRecord();
			longCoffeeRecord.setN("long_coffee");
			longCoffeeRecord.setV(this.coffeeHistoryDescriptor.getLongCount());

			SenMLRecord totalCoffeeRecord = new SenMLRecord();
			totalCoffeeRecord.setN("totale_coffee");
			totalCoffeeRecord.setV(this.coffeeHistoryDescriptor.getTotalCount());

			senMLPack.add(senMLRecord);
			senMLPack.add(shortCoffeeRecord);
			senMLPack.add(mediumCoffeeRecord);
			senMLPack.add(longCoffeeRecord);
			senMLPack.add(totalCoffeeRecord);

			return Optional.of(this.gson.toJson(senMLPack));

		}catch (Exception e){
			return Optional.empty();
		}
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		try{

			//If the request specify the MediaType as JSON or JSON+SenML
			if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON ||
					exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON){

				Optional<String> senmlPayload = getJsonSenmlResponse();

				if(senmlPayload.isPresent())
					exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
				else
					exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
			}
			else
				exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(this.coffeeHistoryDescriptor.toString()), MediaTypeRegistry.TEXT_PLAIN);

		}catch (Exception e){
			exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		try{
			this.coffeeHistoryDescriptor.increaseShortCoffee();
			exchange.respond(ResponseCode.CHANGED);
			changed();
		}catch (Exception e){
			exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void handlePUT(CoapExchange exchange) {

		try{

			String receivedPayload = new String(exchange.getRequestPayload());
			MakeCoffeeRequestDescriptor makeCoffeeRequestDescriptor = this.gson.fromJson(receivedPayload, MakeCoffeeRequestDescriptor.class);

			if(makeCoffeeRequestDescriptor != null && makeCoffeeRequestDescriptor.getType() != null && makeCoffeeRequestDescriptor.getType().length() > 0) {
				if(makeCoffeeRequestDescriptor.getType().equals(MakeCoffeeRequestDescriptor.COFFEE_TYPE_SHORT)) {
					this.coffeeHistoryDescriptor.increaseShortCoffee();
					exchange.respond(ResponseCode.CHANGED);
					changed();
				} else if(makeCoffeeRequestDescriptor.getType().equals(MakeCoffeeRequestDescriptor.COFFEE_TYPE_MEDIUM)){
					this.coffeeHistoryDescriptor.increaseMediumCoffee();
					exchange.respond(ResponseCode.CHANGED);
					changed();
				} else if(makeCoffeeRequestDescriptor.getType().equals(MakeCoffeeRequestDescriptor.COFFEE_TYPE_LONG)){
					this.coffeeHistoryDescriptor.increaseLongCoffee();
					exchange.respond(ResponseCode.CHANGED);
					changed();
				} else
					exchange.respond(ResponseCode.BAD_REQUEST);
			}
			else
				exchange.respond(ResponseCode.BAD_REQUEST);

		}catch (Exception e){
			exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
		}
	}
}
