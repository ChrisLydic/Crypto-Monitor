package com.chrislydic.monitor.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by chris on 12/31/2017.
 */

public class SimplePriceDeserializer implements JsonDeserializer<SimplePrice>
{
	@Override
	public SimplePrice deserialize( JsonElement je, Type type, JsonDeserializationContext jdc)
			throws JsonParseException
	{
		JsonObject content = je.getAsJsonObject();
		String priceKey = content.keySet().iterator().next();
		Double value = content.get( priceKey ).getAsDouble();

		return new SimplePrice( value );
	}
}
