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
package se.sics.dozy.vod.system.config.printer;

import com.typesafe.config.ConfigFactory;
import se.sics.kompics.config.Config;
import se.sics.kompics.config.TypesafeConfig;
import se.sics.ktoolbox.util.profiling.KProfilerKConfig;
import se.sics.ktoolbox.util.profiling.KProfilerRegistryConverter;

public class KProfilerChecker {
    public static void main(String[] args) {
        Config config = TypesafeConfig.load(ConfigFactory.load("alex/nat1/application.conf"));
        KProfilerKConfig kProfilerConfig = new KProfilerKConfig(config);
        System.out.println(KProfilerRegistryConverter.jsonPrettyPrint(kProfilerConfig.kprofileRegistry));
    }
}
