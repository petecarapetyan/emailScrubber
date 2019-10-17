package pete;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    // this class was not written for efficiency or even maintainability

    // before running this you need to remove obviously non-human recipients via a visual edit of the input file
    // example: donotreply@eventbrite.com

    // NEXT: export as .csv

    // TODO: dedupe, sort by name and/or email, concat multinames

    private StringBuilder mainOutput = new StringBuilder();
    private List<String> readFileInList(String fileName) {
        List<String> lines = Collections.emptyList();
        try {
            lines =
                    Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private Map<String, List<String>> clean(List<String> rawList) {
        Map groups = new HashMap<String, List<String>>();
        List emailOnly = new ArrayList<String>();
        List oneEmail = new ArrayList<String>();
        List twoEmail = new ArrayList<String>();
        List multiName = new ArrayList<String>();

        String regex = "^(.+)@(.+)$";

        Pattern pattern = Pattern.compile(regex);
        Iterator<String> itr = rawList.iterator();
        while (itr.hasNext()) {
            int emailCount = 0;
            String[] line = itr.next().trim().split("\\s+");
            int wordCount = line.length;
            for (String temp : line) {
                Matcher matcher = pattern.matcher(temp);
                if (matcher.matches()) {
                    emailCount = emailCount + 1;
                }
            }
            if (emailCount == 1 && wordCount == 3) {
                oneEmail.add(out(line));
            } else if (emailCount == 1 && wordCount == 1) {
                emailOnly.add(out(line));
//                    System.out.println(out(line));
            } else if (emailCount > 1 && wordCount > 3) {
                twoEmail.add(out(line));
            } else if (emailCount > 0) {
                multiName.add(out(line));
            }
        }
        System.out.println("emailOnly=" + emailOnly.size());
        System.out.println("twoEmail=" + twoEmail.size());
        System.out.println("multiName=" + multiName.size());
        System.out.println("oneEmail=" + oneEmail.size());

        groups.put("emailOnly", emailOnly);
        groups.put("twoEmail",twoEmail);
        groups.put("multiName",multiName);
        groups.put("oneEmail",oneEmail);
        return groups;
    }

    private void write(String fileName, String contents) {

        try {
            FileWriter fw = new FileWriter(fileName);
            fw.write(contents);
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private String out(String[] stringArray){
        StringBuilder sb = new StringBuilder();
        for(String dah: stringArray){
            sb.append(dah + ",");
        }
        return sb.toString().substring(0, sb.toString().length()-1);
    }

    private void run(){
        List lines = readFileInList("emailContacts.txt");
        Map<String, List<String>> output = clean(lines);
        Iterator<String> groupIter = output.keySet().iterator();
        while (groupIter.hasNext()) {
            int count = 0;
            int fileCount = 1;
            String name = groupIter.next();
            List<String> namedGroup = output.get(name);
            Iterator<String> innerIter = namedGroup.iterator();
            StringBuilder fileOutput = new StringBuilder();
            while (innerIter.hasNext()) {
                count++;
                String s = innerIter.next();
                // copying the line twice, without last email, in case needed for the two email file
                // will still have to hand edit, this just makes it a touch faster.
                if (name=="twoEmail"){
                    int i = s.lastIndexOf(",");
                    String copy = s;
                    copy = copy.substring(0, i );
                    int j = copy.lastIndexOf(",");
                    copy = copy.substring(0, j )+ "|" + copy.substring(j+1, copy.length());
                    copy= copy.replace("|", ",");
                    fileOutput.append(copy + "\n");
                    s = s.substring(0, j )+ "|" + s.substring(i+1, s.length());
                    s= s.replace("|", ",");
                }else {
                    if (s.indexOf(",") > 0) {
                        int i = s.lastIndexOf(",");
                        s = s.substring(0, i) + "," + s.substring(i + 1, s.length());
                    }
                }
                fileOutput.append(s + "\n");
                if (count>=100){
                    count = 0;
                    String fileName = "JK"+name+fileCount+".csv";
                    fileCount++;
                    write(fileName, fileOutput.toString());
                    fileOutput = new StringBuilder();
                }
            }

            if (count>0){
                count = 0;
                String fileName = name+fileCount+".csv";
                fileCount++;
                write("JK"+fileName, fileOutput.toString());
            }
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.run();
    }


}
