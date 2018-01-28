package com.chrislydic.monitor.network;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by chris on 12/31/2017.
 */

public class PriceDeserializer implements JsonDeserializer<Price>
{
	@Override
	public Price deserialize( JsonElement je, Type type, JsonDeserializationContext jdc)
			throws JsonParseException
	{
		JsonObject content = je.getAsJsonObject().getAsJsonObject( "RAW" );
		String key1 = content.keySet().iterator().next();
		JsonObject innerContent = content.getAsJsonObject( key1 );
		String key2 = innerContent.keySet().iterator().next();
		JsonObject data = innerContent.getAsJsonObject( key2 );

		return new Gson().fromJson(data, Price.class);
	}
}
