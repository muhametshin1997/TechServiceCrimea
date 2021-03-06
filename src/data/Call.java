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
public class Call
{
    private int id;
    private DateTime date;
    private Duration duration;
    private boolean incoming;
    private long phoneNumber;
    private String comment;


    public Call()
    {
    }

    public Call(int id, DateTime date, Duration duration, boolean incoming, long phoneNumber, String comment)
    {
        this.id = id;
        this.date = date;
        this.duration = duration;
        this.incoming = incoming;
        this.phoneNumber = phoneNumber;
        this.comment = comment;
    }

    public static Call parse(String xml)
    {
        if (xml != null && !xml.isEmpty())
            try
            {
                xml = xml.replaceAll("[^\\x20-\\x7e]", " ");
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

                return new Call(Integer.parseInt(element.getAttribute("ID")),
                        DateTime.parse(date, techDateFormatter),
                        Period.parse(period, techPeriodFormatter).toStandardDuration(),
                        Boolean.parseBoolean(element.getAttribute("Incoming")),
                        Long.parseLong(element.getAttribute("PhoneNumber")),
                        element.getTextContent());
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

    public boolean isIncoming()
    {
        return incoming;
    }

    public void setIncoming(boolean incoming)
    {
        this.incoming = incoming;
    }

    public long getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(long phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

}
