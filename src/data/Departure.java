package data;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;

import javax.persistence.*;
import javax.xml.parsers.DocumentBuilderFactory;

import static utils.Utils.techDateFormatter;
import static utils.Utils.techPeriodFormatter;

@Entity
@Table
public class Departure
{
    private int id;
    private DateTime date;
    private Duration duration;
    private String address;
    private String result;

    public Departure()
    {

    }

    public Departure(int id, DateTime date, Duration duration, String address, String result)
    {
        this.id = id;
        this.date = date;
        this.duration = duration;
        this.address = address;
        this.result = result;
    }

    public static Departure parse(String xml)
    {
        if (xml != null && xml.isEmpty())
            try
            {
                xml = xml.replaceAll("[^\\x20-\\x7e]", "");
                Element element = DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder()//Hate JAVA for that!
                        .parse(new ByteArrayInputStream(xml.getBytes("UTF-8")))
                        .getDocumentElement();

                String date = element.getAttribute("Date").split(" ")[0];
                String period = element.getAttribute("Duration");
                if (period.startsWith(":"))
                    period = "0" + period;
                if (period.endsWith(":"))
                    period = period + "0";

                return new Departure(Integer.parseInt(element.getAttribute("ID")),
                        DateTime.parse(date, techDateFormatter),
                        Period.parse(period, techPeriodFormatter).toStandardDuration(),
                        element.getAttribute("Address"),
                        element.getAttribute("Result"));
            } catch (Exception e)
            {
                System.err.println("Request.parse()->\n" + e.toString());
            }
        return null;
    }

    @Id
    @GeneratedValue
    @Column(unique = true, nullable = false)
    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    public DateTime getDate()
    {
        return date;
    }

    public void setDate(DateTime date)
    {
        this.date = date;
    }

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDurationAsString")
    public Duration getDuration()
    {
        return duration;
    }

    public void setDuration(Duration duration)
    {
        this.duration = duration;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    @Override
    public String toString()
    {
        return super.toString();
    }
}
