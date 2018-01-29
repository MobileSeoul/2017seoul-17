package com.kjh.seoulapp.data;

public class ProblemData
{
    public String description;
    public boolean answer;

    public ProblemData(String desc, boolean ans)
    {
        description = desc;
        answer = ans;
    }

    @Override
    public String toString() { return description + ", " + (answer ? "true" : "false"); }
}
