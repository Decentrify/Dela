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

import java.io.PrintWriter;
import java.util.function.BiFunction;
import se.sics.dela.cli.Transfer;
import se.sics.dela.cli.util.ExHelper.DelaException;
import se.sics.dela.cli.util.ExHelper.TrackerException;
import se.sics.ktoolbox.util.trysf.Try;

public class PrintHelper {

  public static <O> int print(PrintWriter out, boolean debug, Try<O> result) {
    if (result.isSuccess()) {
      out.println(result.get().toString());
      return 0;
    } else {
      try {
        ((Try.Failure) result).checkedGet();
      } catch (Throwable ex) {
        if (ex instanceof DelaException) {
          DelaException delaEx = (DelaException) ex;
          if (Transfer.Translate.notReady(result)) {
            out.println("dela service: not running");
          } else {
            out.println("dela exception: " + delaEx.details.toString());
          }
        } else if (ex instanceof TrackerException) {
          TrackerException trackerEx = (TrackerException) ex;
          out.println("tracker exception:" + trackerEx.details.toString());
        } else {
          out.println(ex.getMessage());
          if (debug) {
            out.println("=======DEBUG=======");
            ex.printStackTrace(out);
          }
        }
      }
      return -1;
    }
  }

  public static <O> int print(PrintWriter out, boolean debug, Try<O> result,
    BiFunction<PrintWriter, O, PrintWriter> resultConsumer) {
    if (result.isSuccess()) {
      resultConsumer.apply(out, result.get());
      return 0;
    } else {
      try {
        ((Try.Failure) result).checkedGet();
      } catch (Throwable ex) {
        if (ex instanceof DelaException) {
          DelaException delaEx = (DelaException) ex;
          if (Transfer.Translate.notReady(result)) {
            out.println("dela service: not running");
          } else {
            out.println("dela exception: " + delaEx.details.toString());
          }
        } else if (ex instanceof TrackerException) {
          TrackerException trackerEx = (TrackerException) ex;
          out.println("tracker exception:" + trackerEx.details.toString());
        } else {
          out.println(ex.getMessage());
          if (debug) {
            out.println("=======DEBUG=======");
            ex.printStackTrace(out);
          }
        }
      }
      return -1;
    }
  }
}
