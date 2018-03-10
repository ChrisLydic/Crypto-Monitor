package com.chrislydic.monitor.network;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * Created by chris on 12/31/2017.
 */

public class CoinDeserializer implements JsonDeserializer<CoinList>
{
	@Override
	public CoinList deserialize( JsonElement je, Type type, JsonDeserializationContext jdc )
			throws JsonParseException
	{
		CoinList newCoinList = new Gson().fromJson(je, CoinList.class);

		JsonObject coinData = je.getAsJsonObject().getAsJsonObject( "Data" );
		Set<String> coins = coinData.keySet();

		for (String key : coins) {
			JsonObject coin = coinData.getAsJsonObject( key );

			String imageUrl = Coin.NO_URL;

			if (coin.get( "ImageUrl" ) != null) {
				imageUrl = coin.get( "ImageUrl" ).getAsString();
			}

			// modify sort order because BCH is hidden deep in the coinlist
			if (coin.get( "Symbol" ).getAsString().equals( "BCH" )) {
				newCoinList.addCoin(
						imageUrl,
						"Bitcoin Cash", // cryptocompare gives BCH a weird name
						coin.get( "Symbol" ).getAsString(),
						1
				);
			} else if (coin.get( "Symbol" ).getAsString().equals( "BTC" )) {
				newCoinList.addCoin(
						imageUrl,
						coin.get( "CoinName" ).getAsString(),
						coin.get( "Symbol" ).getAsString(),
						0
				);
			} else {
				newCoinList.addCoin(
						imageUrl,
						coin.get( "CoinName" ).getAsString(),
						coin.get( "Symbol" ).getAsString(),
						coin.get( "SortOrder" ).getAsInt()
				);
			}
		}

		newCoinList.sort();

		return newCoinList;
	}
}
