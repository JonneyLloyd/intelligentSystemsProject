/**
 * Jonathan Lloyd
 * 14117495
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class project {
    public static void main(String args[]) throws IOException {
        Scanner scanner = new Scanner(System.in);
        int generations = 0, population =0, students = 0, totalModules = 0, modulesInCourse = 0,
                simultaneousExams =2, percentages = 100, reproduction = 0, crossover = 0, mutation = 0;
        boolean validEntries = true;
        String output = "";
        try{
            System.out.println("Enter number of generations: ");
            generations = scanner.nextInt();

            System.out.println("Enter population: ");
            population = scanner.nextInt();

            System.out.println("Enter number of students: ");
            students = scanner.nextInt();

            System.out.println("Enter total modules: ");
            totalModules = scanner.nextInt();

            System.out.println("Enter modules in a course: ");
            modulesInCourse = scanner.nextInt();

            if (totalModules <= 0 || modulesInCourse <= 0 || students <= 0 || population <= 0 || generations <= 0)
                validEntries = false;
            else
            {
                System.out.println("Enter % for reproduction: ");
                reproduction = scanner.nextInt();
                percentages -= reproduction;

                System.out.println("Enter % for crossover: ");
                crossover = scanner.nextInt();
                percentages -= crossover;

                System.out.println("Enter % for mutation: ");
                mutation = scanner.nextInt();
                percentages -= mutation;

                if (percentages != 0 || reproduction < 5 || crossover < 5 || mutation < 5){
                    System.out.println("Total value of settings must equal 100% and minimum value is 5%\n" +
                            "Using default values of 80%, 15%, 5% for reproduction, crossover, mutation respectively");
                    reproduction = 80;
                    crossover = 15;
                    mutation = 5;
                }
            }
        }
        catch(InputMismatchException e){
            validEntries = false;
        }
        if(!validEntries){
            System.out.println("Invalid input detected, all entries must be integers greater than 0");
        }
        else if (totalModules <= modulesInCourse){
            System.out.println("Total modules must be greater than number of modules in a course ");
        }

        else{
            /*
            Using a list of integers to represent all modules. This makes it possible to
            use collection sort instead of constantly generating random numbers
            and guarantees no repeat numbers
            */
            List<Integer> modules = new ArrayList<>();
            for (int i = 1; i <= totalModules; i++) {
                modules.add(i);
            }

            ArrayList<Set<Integer>> studentModules = new ArrayList<>();
            generateStudentsTable(studentModules, modules, students, modulesInCourse);

            ArrayList<int[][]> orderings = new ArrayList<>();
            generateOrderings(orderings, totalModules, simultaneousExams, population, modules);

            fitnessFunction(studentModules, orderings);

            printToFile(studentModules, orderings);

            //TODO remove after testing
            reorder(orderings);

            //TODO remove after testing
            printToScreen(orderings);

            int rep = (int)(((double)reproduction /100) * orderings.size());
            int cross = (int)(((double)crossover/100) * orderings.size());
            int mut = (int)Math.ceil(((double)mutation/100) * orderings.size());

            System.out.println("Rep :" + rep + " cross :" + cross +" mut :" + mut);
            /*
            randomOrderings is a List of ints that correspond to the orderings ArrayList
            this is used to shuffle the orderings using collections without having to operate
            on the objects to shuffle. By iterating over this it guarantees no index used twice
            */
            List<Integer> randomOrderings = new ArrayList<>();
            for (int j = 0; j < orderings.size(); j++) {
                randomOrderings.add(j);
            }

            for(int i = 0; i < generations; i++) {
                System.out.println("Start " + orderings.size());
                Collections.shuffle(randomOrderings);

                for (int j = 0; j < randomOrderings.size(); j++)
                    System.out.println(j + " - " + randomOrderings.get(j));


                //reproduction(orderings, rep, randomOrderings);

                /*
                As I use same arrayList for orderings do nothing to number of orderings in reproduction
                This means they are put in next generation with no change. These orderings are not operatated on
                By either of the other functions due to use of randomOrderings List.
                 */
                System.out.println("After Reprodution!! " + orderings.size());
                printToScreen(orderings);

                //crossover on random orderings not used in reproduction
                for (int j = rep; j < (rep+cross); j++) {
                    System.out.println("J is " + j + " random is " + randomOrderings.get(j) + " and "+ randomOrderings.get(j+1));
                    System.out.println("1st Crossover before");
                    printSingleOrderingToScreen(orderings.get(randomOrderings.get(j)));
                    //System.out.println("2nd Crossover before");
                    //printSingleOrderingToScreen(orderings.get(randomOrderings.get(j+1)));
                    crossover(orderings, randomOrderings.get(j), randomOrderings.get(j+1));
                    fitnessFunction(studentModules, orderings.get(randomOrderings.get(j)));
                    fitnessFunction(studentModules, orderings.get(randomOrderings.get(j + 1)));
                    System.out.println("1st Crossover after");
                    printSingleOrderingToScreen(orderings.get(randomOrderings.get(j)));
                    //System.out.println("2dn Crossover after");
                    //printSingleOrderingToScreen(orderings.get(randomOrderings.get(j+1)));
                }
                System.out.println("After Crossover!! " + orderings.size());
                printToScreen(orderings);

                //mutation on random orderings not user in reproduction or crossover
                for (int j = rep+cross; j < randomOrderings.size(); j++) {
                    System.out.println("Mutation on " + j + " random is " + randomOrderings.get(j));
                    mutation(orderings, randomOrderings.get(j));
                    fitnessFunction(studentModules, orderings.get(randomOrderings.get(j)));
                }

                System.out.println("After Mutation!! " + orderings.size());
                printToScreen(orderings);

                reorder(orderings);
//                //remove worst results to bring size back to original population
//                for (int j = orderings.size()-1; j >= population; j--){
//                    orderings.remove(j);
//                }
                System.out.println("After reorder!! " + orderings.size());
                printToScreen(orderings);
            }

            System.out.println("Best result is: ");
            printSingleOrderingToScreen(orderings.get(0));
        }
    }

    //fitness function
    public static void fitnessFunction(ArrayList<Set<Integer>> studentModules, ArrayList<int[][]>orderings)
    {
        for (int i = 0; i < orderings.size(); i++){
            for (int j = 0; j < orderings.get(i)[0].length-1; j++){ //-1 as last column is for fitness
                for(int s = 0; s < studentModules.size(); s++){
                    if(studentModules.get(s).contains(orderings.get(i)[0][j]) && studentModules.get(s).contains(orderings.get(i)[1][j])){
                        orderings.get(i)[0][orderings.get(i)[0].length -1] += 1; //increment fitness value
                    }
                }
            }
        }
    }

    /*
    fitness function for recalculating individual orderings. Resets fitness to 0 first
    */
    public static void fitnessFunction(ArrayList<Set<Integer>> studentModules, int[][]ordering)
    {
        ordering[0][ordering[0].length -1] = 0;
            for (int j = 0; j < ordering[0].length-1; j++){ //-1 as last column is for fitness
                for(int s = 0; s < studentModules.size(); s++){
                    if(studentModules.get(s).contains(ordering[0][j]) && studentModules.get(s).contains(ordering[1][j])){
                        ordering[0][ordering[0].length -1] += 1; //increment fitness value
                    }
                }
            }
    }

    /*
    generating students table using shuffle on modules list
    */
    public static void generateStudentsTable(ArrayList<Set<Integer>> studentModules, List<Integer> modules, int students, int modulesInCourse){
        for(int i = 0; i < students; i++){
            Collections.shuffle(modules);
            Set<Integer> set = new HashSet<>();
            for (int j = 0; j < modulesInCourse;){
                set.add(modules.get(j));
                j++;
            }
            studentModules.add(set);
        }
    }

    /*
    generate orderings
    */
    public static void generateOrderings(ArrayList<int[][]>orderings, int totalModules,
                                                       int simultaneousExams, int population, List<Integer> modules){
        for(int i = 0; i < population; ){
            int[][] set = new int [simultaneousExams][(totalModules/2)+1]; //+1 for fitness & null
            boolean duplicate = false;
            Collections.shuffle(modules);
            int count = 0, duplicatesFound = 0;
            for (int j = 0; j < set.length; j++){
                for (int k =0; k < (totalModules/2); k++){
                    /*
                    iterate up through modules list instead of popping records
                    this saves reinitialising List. Only need to shuffle again
                    */
                    set[j][k] = modules.get(count);
                    count ++;
                }
            }
            //checking that generated array is not a duplicate
           for(int u = 0; u < i && !duplicate; u++){
               if (Arrays.deepEquals(orderings.get(u), set)) {
                   duplicate = true;
                   duplicatesFound++;
               }
           }
            //only add set to orderings if its unique
            if (!duplicate) {
                orderings.add(set);
                duplicatesFound = 0;
                i++;
            }
            //check to make sure not stuck in infinite loop. i.e no more unique orderings can be generated
            if(duplicatesFound > 10){
                System.out.println("Only able to create " + i + " unique orderings");
                break;
            }
        }
    }

    public static void crossover(ArrayList<int[][]>orderings, int location1, int location2 ){
        int [][] ordering1 = new int[orderings.get(location1).length][];
        for(int j = 0; j < orderings.get(location1).length; j++)
            ordering1[j] = orderings.get(location1)[j].clone();

        int [][] ordering2 = new int[orderings.get(location2).length][];
        for(int j = 0; j < orderings.get(location2).length; j++)
            ordering2[j] = orderings.get(location2)[j].clone();

       //System.out.println("1st Crossover before");
        //printSingleOrderingToScreen(ordering1);
        int row = 0, col = 0, temp = 0;
        int randomNum = ThreadLocalRandom.current().nextInt(1, (ordering1[0].length-2)*2);
        //System.out.println("Random Num is " + randomNum);
        for (int i = 0; i <= randomNum; i++){
            col = i % (ordering1[0].length-1);
            //System.out.println("COL is " + col);
            if(i == ordering1[0].length-1) {
                row = 1;
             }
            //System.out.println("order1 " +  ordering1[row][col] + " and order2 is " + ordering2[row][col]);
            temp = ordering1[row][col];
            //System.out.println("Temp is " + temp);
            ordering1[row][col] = ordering2[row][col];
           // System.out.println("order1 changed " +  ordering1[row][col]);
            ordering2[row][col] = temp;
            //System.out.println("order2 changed " +  ordering2[row][col]);
        }
        //System.out.println("Finished swapping");
        boolean found = false;
        ArrayList<Integer> list1Dup = new ArrayList<>();
        ArrayList<Integer> list2Dup = new ArrayList<>();
        for (int i = 0; i < ordering1.length; i++) {
            for (int j = 0; j < ordering1[i].length - 1; j++) {
                if(list1Dup.contains(ordering1[i][j])) {
                    //System.out.println("Found duplicate");
                    found = false;
                    //found duplicate in first list so swap with first duplicate found in second list
                    for (int k = 0;k < ordering2.length && !found; k++) {
                        for (int l = 0; l < ordering2[k].length - 1; l++) {
                            if(list2Dup.contains(ordering2[k][l])) {
                                //System.out.println("Found inner duplicate");
                                temp = ordering1[i][j];
                                ordering1[i][j] = ordering2[k][l];
                                ordering2[k][l] = temp;
                                found = true;
                                list2Dup.clear();
                            }
                            else
                                list2Dup.add( ordering2[k][l]);
                        }
                    }
                }
                else
                    list1Dup.add( ordering1[i][j]);
            }
        }
       // System.out.println("1st Crossover after");
        //printSingleOrderingToScreen(ordering1);
        /*
        System.out.println("1st Crossover before");
        printSingleOrderingToScreen(orderings.get(location1));
        System.out.println("1st Crossover after");
        printSingleOrderingToScreen(ordering1);

        System.out.println("2nd Crossover before");
        printSingleOrderingToScreen(orderings.get(location2));
        System.out.println("2nd Crossover after");
        printSingleOrderingToScreen(ordering2);
        */
        orderings.set(location1, ordering1);
        orderings.set(location2, ordering2);

        //System.out.println("1st Crossover after insert");
       // printSingleOrderingToScreen(orderings.get(location1));
    }

    /*
    Reproduction will add "index" amount of duplicates to the orderings
    these are taken from the top of the randomOrderings which are already shuffled indexes for orderings
    */
    public static void reproduction( ArrayList<int[][]>orderings, int index, List<Integer> randomOrderings ){
        for (int i = 0; i < index; i++){
            int [][] myInt = new int[orderings.get(i).length][];
            for(int j = 0; j < orderings.get(i).length; j++)
                myInt[j] = orderings.get(randomOrderings.get(i))[j].clone();
            orderings.add(myInt);
            System.out.println("Reproduction:");
            System.out.println(i+1 + " - ordering - " + (randomOrderings.get(i)+1));
        }
    }

    /*
    Custom comparator to reorder orderings ascending on fitness cost
    */
    public static void reorder( ArrayList<int[][]>orderings) {
        Collections.sort(orderings, new Comparator<int[][]>() {
            public int compare(int[][] a, int[][] b) {
                return a[0][a[1].length - 1] - b[0][b[1].length - 1];
            }
        });
    }

    /*
    Perform mutation on ordering at index held in location
     */
    public static void mutation(ArrayList<int[][]>orderings, int location){
        int [][] ordering = new int[orderings.get(location).length][];
        for(int j = 0; j < orderings.get(location).length; j++)
            ordering[j] = orderings.get(location)[j].clone();

        int randomNum1, randomNum2, temp;
        //upto not including ordering.length as last column reserved for fitness
        randomNum1 = ThreadLocalRandom.current().nextInt(0, ordering.length );
        randomNum2 = ThreadLocalRandom.current().nextInt(0, ordering.length );
        while(randomNum1 == randomNum2){
            randomNum2 = ThreadLocalRandom.current().nextInt(0, ordering.length);
        }
        temp = ordering[0][randomNum1];
        ordering[0][randomNum1] = ordering[1][randomNum2];
        ordering[1][randomNum2] = temp;
        orderings.set(location, ordering);
    }

    /*
    Print student modules and orderings to text file
     */
    public static void printToFile(ArrayList<Set<Integer>> studentModules, ArrayList<int[][]>orderings) throws  IOException{
        String output = "";
        File file = new File("AI17.txt");
        file.createNewFile();
        FileWriter writer = new FileWriter(file);

        //Looping through students and putting on output string
        for (int i = 0; i < studentModules.size(); i++) {
            output += "Student "+ (i+1) + ": ";
            Iterator<Integer> iterator = studentModules.get(i).iterator();
            while(iterator.hasNext()) {
                output += "M" + iterator.next() + " ";
            }
            output += "\n";
        }
        output += "\n";

        //Looping through orderings and putting on output string
        for (int i = 0; i < orderings.size(); i++) {
            output += "\nOrd" + (i + 1) + ":";
            for (int j = 0; j < orderings.get(i).length; j++) {
                for (int k = 0; k < orderings.get(i)[j].length - 1; k++) {
                    output += "\tm"+ orderings.get(i)[j][k];
                }
                if(j == orderings.get(i).length -1)
                    output += "\tcost: " + orderings.get(i)[0][orderings.get(i)[0].length - 1];
                output += "\n\t";
            }
        }
        output += "\n";
        //writing output string to file
        writer.write(output);
        writer.flush();
        writer.close();
    }

    /*
    Print orderings to screen
    Kept this function for debug/testing purposes
    */
    public static void printToScreen(ArrayList<int[][]>orderings){
        String output = "";
        for (int i = 0; i < orderings.size(); i++) {
            output += "\nOrd" + (i + 1) + ":";
            for (int j = 0; j < orderings.get(i).length; j++) {
                for (int k = 0; k < orderings.get(i)[j].length - 1; k++) {
                    output += "\tm"+ orderings.get(i)[j][k];
                }
                if(j == orderings.get(i).length -1)
                    output += "\tcost: " + orderings.get(i)[0][orderings.get(i)[0].length - 1];
                output += "\n\t";
            }
        }
        output += "\n";
        System.out.println(output);
    }

    /*
    Print single ordering to screen
    */
    public static void printSingleOrderingToScreen(int[][] ordering){
        String output = "";
            for (int j = 0; j < ordering.length; j++) {
                for (int k = 0; k < ordering[j].length - 1; k++) {
                    output += "\tm"+ ordering[j][k];
                }
                if(j == ordering.length -1)
                    output += "\tcost: " + ordering[0][ordering[0].length - 1];
                output += "\n";
            }

        output += "\n";
        System.out.println(output);
    }
}
