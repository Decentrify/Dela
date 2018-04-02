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

import com.beust.jcommander.Parameter;
import se.sics.dela.cli.Tracker;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class DownloadCmd {
  @Parameter(description = "tracker used", names = "-target")
  public String target = Tracker.Target.BBC5_TEST;
  @Parameter(description = "public datasetId", names = "-dataset", required = true)
  public String datasetId;
  @Parameter(description = "datasetName to name to save it under", names = "-name", required = true)
  public String datasetName;
}