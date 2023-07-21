/*
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information.
 *
 * This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.upnprouter.internal;

import static org.openhab.binding.upnprouter.internal.UPnpRouterBindingConstants.THING_TYPE_ROUTER;
import static org.openhab.binding.upnprouter.internal.UPnpRouterHandlerFactory.SUPPORTED_THING_TYPES_UIDS;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

import org.jupnp.model.meta.RemoteDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dmitry P. (d51x) - Initial contribution
 */
@Component(service = { UpnpDiscoveryParticipant.class })
@NonNullByDefault
public class UPnpRouterDiscovery  implements UpnpDiscoveryParticipant  {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice remoteDevice) {
        DiscoveryResult result = null;
        ThingUID thingUid = getThingUID(remoteDevice);
        if (thingUid != null) {
            String label = remoteDevice.getDetails().getFriendlyName().isEmpty()
                        ? remoteDevice.getDisplayString()
                        : remoteDevice.getDetails().getFriendlyName() + " " +
                          remoteDevice.getDetails().getModelDetails().getModelNumber();

            Map<String, Object> properties = new HashMap<>();
            properties.put("Model", remoteDevice.getDetails().getModelDetails().getModelNumber());
            properties.put("Manufacturer", remoteDevice.getDetails().getManufacturerDetails().getManufacturer());
            properties.put("udn", remoteDevice.getIdentity().getUdn().getIdentifierString());
            properties.put("Serial Number", remoteDevice.getDetails().getSerialNumber());
            properties.put("Base url", remoteDevice.getDetails().getPresentationURI().getHost());

            result = DiscoveryResultBuilder.create(thingUid)
                 .withLabel(label)
                 .withProperties(properties)
                 .withRepresentationProperty("udn")
                 .build();
        }
        return result;
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice remoteDevice) {
        ThingUID result = null;
        String deviceType = remoteDevice.getType().getType();
        // "urn:schemas-upnp-org:device:InternetGatewayDevice:1"
        if (deviceType.equalsIgnoreCase("InternetGatewayDevice")) {
            logger.info("Device type {}, manufacturer {}, model {}, SN# {}, UDN {}",
                    deviceType, remoteDevice.getDetails().getManufacturerDetails().getManufacturer(),
                    remoteDevice.getDetails().getModelDetails().getModelName(),
                    remoteDevice.getDetails().getSerialNumber(),
                    remoteDevice.getIdentity().getUdn().getIdentifierString());
            ThingTypeUID thingTypeUID = THING_TYPE_ROUTER;
            result = new ThingUID(thingTypeUID, remoteDevice.getIdentity().getUdn().getIdentifierString());
        }
        return result;
    }
}