/*
 * Copyright (C) 2010 Teleal GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.teleal.cling.model.message.header;

import java.net.URI;

import org.teleal.cling.model.types.ServiceType;

/**
 * @author Christian Bauer
 */
public class ServiceTypeHeader extends UpnpHeader<ServiceType> {

    public ServiceTypeHeader() {
    }

    public ServiceTypeHeader(URI uri) {
        setString(uri.toString());
    }

    public ServiceTypeHeader(ServiceType value) {
        setValue(value);
    }

    public void setString(String s) throws InvalidHeaderException {
        try {
            setValue(ServiceType.valueOf(s));
        } catch (RuntimeException ex) {
            throw new InvalidHeaderException("Invalid service type header value, " + ex.getMessage());
        }
    }

    public String getString() {
        return getValue().toString();
    }
}