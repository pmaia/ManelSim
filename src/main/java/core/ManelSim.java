/**
 * Copyright (C) 2009 Universidade Federal de Campina Grande
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class ManelSim {

	public static void main(String[] args) 
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		if(args.length < 1) {
			System.out.println("Usage: ManelSim <conf file>");
			System.exit(1);
		}
		
		Properties config = new Properties();
		config.load(new FileInputStream(args[0]));
		
		if(!config.containsKey("initializer") || !config.containsKey("summarizer")) {
			throw new IllegalArgumentException("You must specify the keys \"initializer\" and \"summarizer\" " +
					"in the configuration file.");
		}
		
		String initializerClassName = config.getProperty("initializer");
		String summarizerClassName = config.getProperty("summarizer");
		
		Initializer initializer = (Initializer)Class.forName(initializerClassName).newInstance();
		Summarizer summarizer = (Summarizer)Class.forName(summarizerClassName).newInstance();
		
		Object context = initializer.initialize(config);
		System.out.println(summarizer.summarize(context));
		
	}		

}
