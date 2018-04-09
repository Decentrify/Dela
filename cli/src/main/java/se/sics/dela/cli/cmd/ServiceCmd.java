/*
 * Copyright (C) 2016 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2016 Royal Institute of Technology (KTH)
 *
 * Dozy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.dela.cli.cmd;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
@Parameters(commandDescription = "Dela daemon service. Options: START/STOP/STATUS")
public class ServiceCmd {

  @Parameter(description = "option")
  public String val;

  public static enum Service {

    STATUS,
    START,
    STOP
  }

  public Service value() throws ParameterException {
    try {
      Service convertedValue = Service.valueOf(val.toUpperCase());
      return convertedValue;
    } catch (IllegalArgumentException ex) {
      throw new ParameterException("Value: " + val + 
        " is not a valid option. Available options are: START/STOP/STATUS");
    }
  }

  //Converters don't work on main parameter in JCommander 1.71
  public static class ServiceConverter implements IStringConverter<Service> {

    @Override
    public Service convert(String value) {
      try {
        Service convertedValue = Service.valueOf(value.toUpperCase());
        return convertedValue;
      } catch (IllegalArgumentException ex) {
        throw new ParameterException("Value " + value + "is not a valid option"
          + "Available values are: START/STOP/STATUS");
      }
    }
  }
}
