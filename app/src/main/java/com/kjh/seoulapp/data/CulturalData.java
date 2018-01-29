package com.kjh.seoulapp.data;

public class CulturalData {
    public String title;
    public String content;
    public String prob1;
    public String prob2;
    public String prob3;
    public boolean ans1;
    public boolean ans2;
    public boolean ans3;
    public double latitude;
    public double longitude;

	@Override
	public String toString()
	{
		return title + content.substring(0,5) +
				prob1.substring(0,5) + prob2.substring(0,5) + prob3.substring(0,5) +
				ans1 + ans2 + ans3 +
				latitude + "," + longitude;
	}
}
