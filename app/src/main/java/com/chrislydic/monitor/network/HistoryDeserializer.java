package com.chrislydic.monitor.network;

import com.chrislydic.monitor.network.History;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by chris on 12/31/2017.
 */

public class HistoryDeserializer implements JsonDeserializer<History>
{
	@Override
	public History deserialize( JsonElement je, Type type, JsonDeserializationContext jdc)
			throws JsonParseException
	{
		History newHistory = new Gson().fromJson(je, History.class);

		JsonArray times = je.getAsJsonObject().getAsJsonArray( "Data" );
		for (JsonElement time : times) {
			newHistory.addTime(
					time.getAsJsonObject().get( "time" ).getAsLong(),
					time.getAsJsonObject().get( "close" ).getAsDouble(),
					time.getAsJsonObject().get( "high" ).getAsDouble(),
					time.getAsJsonObject().get( "low" ).getAsDouble(),
					time.getAsJsonObject().get( "open" ).getAsDouble(),
					time.getAsJsonObject().get( "volumefrom" ).getAsDouble(),
					time.getAsJsonObject().get( "volumeto" ).getAsDouble()
			);
		}

		return newHistory;
	}
}
