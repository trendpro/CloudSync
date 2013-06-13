package com.cloudSync;

/*
 * A model class representing a list item.
 */
public class ListItem 
{
	//variable declaration
	private String itemTitle;
	private String itemType;
	private String key;
	
	public ListItem(String title,String key,String type)
	{
		itemTitle = title;
		itemType = type;
		this.key = key;
	}
	
	public String getItemTitle()
	{
		return itemTitle;
	}

	public String getItemType()
	{
		return itemType;
	}
	
	public void setItemTitle(String itemTitle)
	{
		this.itemTitle = itemTitle;
	}
	
	public void setItemType(String itemType)
	{
		this.itemType = itemType;
	}
	
	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}
}
