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
import com.beust.jcommander.Parameters;
import se.sics.dela.cli.Tracker;

/**
 *
 * @author Alex Ormenisan <aaor@kth.se>
 */
@Parameters(commandDescription="Search for datasets")
public class SearchCmd {
  @Parameter(description = "Tracker used", names = "-target", hidden = true)
  public String target = Tracker.Target.HOPS;
  @Parameter(description = "Search term", names = "-term", required = true)
  public String term;
}
