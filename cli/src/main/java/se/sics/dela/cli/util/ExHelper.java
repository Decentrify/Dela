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
package se.sics.dela.cli.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.function.BiFunction;
import java.util.function.Function;
import se.sics.dela.cli.dto.ErrorDescDTO;
import se.sics.dela.cli.dto.JsonResponse;
import se.sics.ktoolbox.util.trysf.Try;
import static se.sics.ktoolbox.util.trysf.TryHelper.tryFFail;

public class ExHelper {

  public static Function<String, Throwable> simpleTrackerExMapper() {
    return (String stringEx) -> {
      try {
        JsonResponse exDesc = new Gson().fromJson(stringEx, JsonResponse.class);
        return new TrackerException(exDesc);
      } catch (JsonSyntaxException ex) {
        return new UnknownClientException(stringEx, ex);
      }
    };
  }

  public static Function<String, Throwable> simpleDelaExMapper() {
    return (String stringEx) -> {
      try {
        ErrorDescDTO exDesc = new Gson().fromJson(stringEx, ErrorDescDTO.class);
        return new DelaException(exDesc);
      } catch (JsonSyntaxException ex) {
        return new UnknownClientException(stringEx, ex);
      }
    };
  }

  public static class TrackerException extends Exception {

    public final JsonResponse details;

    public TrackerException(JsonResponse exDesc) {
      super("server exception");
      this.details = exDesc;
    }
  }

  public static class DelaException extends Exception {

    public final ErrorDescDTO details;

    public DelaException(ErrorDescDTO details) {
      super();
      this.details = details;
    }
  }

  public static class ClientException extends Exception {

    public ClientException(String msg, Throwable cause) {
      super(msg, cause);
    }

    public ClientException(Throwable cause) {
      super(cause);
    }
  }

}
