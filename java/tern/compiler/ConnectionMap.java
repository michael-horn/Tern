/*
 * @(#) ConnectionMap.java
 * 
 * Tern Tangible Programming System
 * Copyright (C) 2009 Michael S. Horn 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tern.compiler;

import java.util.List;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import topcodes.*;


/**
 * This class facilitates the process of pairing sockets and
 * connectors of statements in a tangible program. 
 * 
 * @author Michael Horn
 * @version $Revision: 1.1 $, $Date: 2007/06/23 16:08:24 $
 */
public class ConnectionMap {

	protected List sockets;
	protected List connectors;
	
	
	
	/**
	 * Default constructor.  Creates an empty connection map.
	 */
	public ConnectionMap() {
		this.sockets = new java.util.ArrayList();
		this.connectors = new java.util.ArrayList();
	}
	
	
	
	/**
	 * Clears all registered parameters, sockets, and connectors.
	 */
	public void clear() {
		this.sockets.clear();
		this.connectors.clear();
	}
	
	
	
	/**
	 * Adds a socket location to the connection map and links it back
	 * to its originating statement.
	 */
	public void addSocket(Statement statement, double dx, double dy) {
		TopCode top = statement.getTopCode();
		if (top == null) return;
		double unit = top.getDiameter();
		double theta = top.getOrientation();
		double x = top.getCenterX();
		double y = top.getCenterY();
		x += (dx * unit * Math.cos(theta) -
			  dy * unit * Math.sin(theta));
		y += (dx * unit * Math.sin(theta) +
			  dy * unit * Math.cos(theta));
		this.sockets.add(new TSocket(statement, x, y, unit * 0.8));
	}
	
	
	
	/**
	 * Adds a connector location to the connection map and links it back
	 * to its originating statement.
	 */
	public void addConnector(Statement statement, String name,
							 double dx, double dy) {
		TopCode top = statement.getTopCode();
		if (top == null) return;
		double unit = top.getDiameter();
		double theta = top.getOrientation();
		double x = top.getCenterX();
		double y = top.getCenterY();
		x += (dx * unit * Math.cos(theta) -
			  dy * unit * Math.sin(theta));
		y += (dx * unit * Math.sin(theta) +
			  dy * unit * Math.cos(theta));
		this.connectors.add(new TConnector(statement, name, x, y));
	}
	
	
	
	/**
	 * Uses an inefficient (O(n^2)) algorithm to find and connect all
	 * sockets and connectors that overlap in the digital image.
	 */
	public void formConnections() {
		TSocket s;
		TConnector c;
		for (int j=0; j<connectors.size(); j++) {
			c = (TConnector)connectors.get(j);
			for (int i=0; i<sockets.size(); i++) {
				s = (TSocket)sockets.get(i);
				if (!c.attached && s.isConnectedTo(c)) {
					c.attached = true;
					c.statement.connect(s.statement, c.name);
					break;
				}
			}
		}
	}
	
	
	
	/**
	 * For debugging.  Outlines all registered sockets and connectors in
	 * an image.
	 */
	public void draw(Graphics2D g) {
		TSocket s;
		TConnector c;
		Ellipse2D circ;
		g.setColor(Color.GREEN);
		for (int i=0; i<sockets.size(); i++) {
			s = (TSocket)sockets.get(i);
			circ = new Ellipse2D.Double(s.x - s.r, s.y - s.r,
										s.r*2, s.r*2);
			g.draw(circ);
		}
		
		g.setColor(Color.RED);
		for (int i=0; i<connectors.size(); i++) {
			c = (TConnector)connectors.get(i);
			circ = new Ellipse2D.Double(c.x - 10, c.y - 10, 20, 20);
			g.fill(circ);
		}
	}
	
	
	
	//-------------------------------------------------------
	// Inner class: TSocket
	//-------------------------------------------------------
	class TSocket {
		public double x;
		public double y;
		public double r;
		public Statement statement;
		
		public TSocket(Statement s, double x, double y, double r) {
			this.statement = s;
			this.x = x;
			this.y = y;
			this.r = r;
		}
		
		public boolean isConnectedTo(TConnector c) {
			double dx = (c.x - this.x);
			double dy = (c.y - this.y);
			return ((dx * dx + dy * dy) <= (r * r));
		}
	}
	
	
	
	//-------------------------------------------------------
	// Inner class: TConnector
	//-------------------------------------------------------
	class TConnector {
		public double x;
		public double y;
		public String name;
		public Statement statement;
		public boolean attached;
		
		public TConnector(Statement statement, String name,
						  double x, double y) {
			this.statement = statement;
			this.name = name;
			this.x = x;
			this.y = y;
			this.attached = false;
		}
	}
}
