// COPYRIGHT_BEGIN
//
// Copyright (C) 2000-2008  Wizzer Works (msm@wizzerworks.com)
// 
// This file is part of the M3G Toolkit.
//
// The M3G Toolkit is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
//
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
// COPYRIGHT_END

// Declare package.
package com.wizzer.m3g.toolkit.util;

// Import standard Java classes.
import java.lang.IllegalArgumentException;
import java.util.LinkedList;

// Import M3G Toolkit classes.
import com.wizzer.m3g.Node;
import com.wizzer.m3g.Group;

/**
 * A utility to determine paths in the scene graph.
 * 
 * @author Mark Millard
 */
public class Path
{
	/** The current node in the scene graph. */
	protected Node m_node;
	
	// Hide the default constructor.
	private Path() {}
	
	/**
	 * Construct a path for the specified node.
	 * 
	 * @param node The node to root the path to.
	 */
	public Path(Node node)
	{
		if (node == null)
			throw new IllegalArgumentException();
		
		m_node = node;
	}
	
	/**
	 * Get the list of nodes from this Scene Graph element to the specified
	 * target.
	 * 
	 * @param target The Scene Graph element to find the path to.
	 * 
	 * @return An array of Nodes is returned where the first element is closest
	 * to the current node and the last element is closest to the target. There
	 * should be no duplicate Nodes in the array.
	 */
	public Node[] getPathTo(Node target)
	{
		LinkedList stack = new LinkedList();
		boolean found = buildPathTo(m_node, target, stack, null);
		if ((! found) || stack.isEmpty())
			return null;
		else
			return (Node[])stack.toArray();
	}
	
	// Do the work of determining a path from curNode to target.
	// The path will be an array of Nodes where the first element is closest
	// to the current node and the last element is closest to the target.
	private boolean buildPathTo(Node curNode, Node target, LinkedList stack, Node ignoreChild)
	{
		// Determine if the current node is the one we are looking for.
		if (curNode.equals(target))
		{
			stack.addFirst(curNode);
			return true;
		}
		
		// Determine if there are children to process.
		if (curNode instanceof Group)
		{
			Group group = (Group)curNode;
			int numChildren = group.getChildCount();
			for (int i = 0; i < numChildren; i++)
			{
				Node child = group.getChild(i);
				if ((ignoreChild != null) && (child.equals(ignoreChild)))
					continue;
				
				boolean found = buildPathTo(child, target, stack, null);
				if (found)
				{
					stack.addFirst(curNode);
					return true;
				}
			}
		}
		
		// Determine if path should include parent node.
		Node parent = curNode.getParent();
		if (parent != null)
		{
			boolean found = buildPathTo(parent, target, stack, curNode);
			if (found)
			{
				stack.addFirst(curNode);
				return true;
			}
		}

		return false;
	}
}
