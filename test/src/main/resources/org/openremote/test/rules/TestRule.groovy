package org.openremote.test.rules

import com.fasterxml.jackson.databind.node.ObjectNode
import org.openremote.manager.rules.RulesBuilder
import org.openremote.model.geo.GeoJSONPoint
import org.openremote.model.query.AssetQuery
import org.openremote.model.rules.AssetState
import org.openremote.model.rules.Assets

import java.util.logging.Logger

Logger LOG = binding.LOG
RulesBuilder rules = binding.rules
Assets assets = binding.assets

String msgAttr = "gps_payload"
String locationAttr = "location"

AssetQuery query = new AssetQuery().attributeName(msgAttr)

rules.add()
        .name("Update GEOJSON points")
        .when({
            facts ->
                return facts.matchLastAssetEvent(query).isPresent()
        })
        .then({ facts ->
            facts.matchAssetEvent(query)
                    // Filter for events for gps_payload attrs
                    .filter({
                        it -> it.fact.name == msgAttr
                    })
                    .forEach({
                        it ->
                            AssetState state = it.fact;

                            ObjectNode node = state.value.get() as ObjectNode

                            Double lat = node.get("lt").asDouble()
                            Double lng = node.get("lg").asDouble()

                            assets.dispatch(state.id, locationAttr, new GeoJSONPoint(lng, lat))

                            // Remove state from facts to only handle it once
                            facts.getAssetEvents().removeIf({
                                other ->
                                    other.fact == state;
                            })
                    })
            LOG.info("Updated tracker locations")
        })
