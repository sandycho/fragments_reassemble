package fragment.submissions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        try ( BufferedReader in = new BufferedReader( new FileReader( args[0] ) ) ) {

            in.lines().map(Reassembler::reassemble).forEach(System.out::println);

        }catch( Exception e ){

        }
    }

    public static class Reassembler {
        private static int MIN_CHARS_TO_MATCH = 2;

        /**
         * Rebuilds a text from a bunch of pieces of text.
         *
         * @param fragments concats by ";" char
         * @return a string with the reassembled text
         */
        public static String reassemble( String fragments ){
            String result = null, currentFrag = null, matchFrag = null;
            Set fragmentsSet = new HashSet();

            HashMap<Integer, HashMap> map = new HashMap<>();;
            HashMap newMatch = null, fragMatchStruct = null;
            HashMap tempStruct = null, tempNewMatchInv = null, tempNewMatchRev = null, tempNewMatchCon = null;

            int index = 0;

            try{
                ArrayList<String> fragList = new ArrayList<>( Arrays.asList( fragments.split( ";" ) ) );

                // iterates the list matching fragments until to get a unique chain with the reassembled text
                while( fragList.size() > 1 ){
                    tempStruct = null;

                    // gets the current fragment to compare to the list
                    currentFrag = fragList.get( index );
                    fragMatchStruct = map.get(index);

                    // iterates over the fragment list keeping every match in the current fragment matches structure in a map
                    // the followin lines show the struct after the first iteration
                    // wns a clown;owns ;He owns a
                    // map = {
                    //      0 = {
                    //          matchInv = { chain="wns a clowns", overlap="3", fragId="1" },
                    //          matchRev = { chain="He owns a clown", overlap="5", fragId="2" },
                    //          matchCon = null,
                    //      },
                    //      1 = {
                    //          matchInv = { chain="owns a clown", overlap="4", fragId="0" },
                    //          matchRev = { chain="wns a clowns ", overlap="3", fragId="0" },
                    //          matchCon = { chain="He owns a", overlap="5", fragId="2" },
                    //      },
                    //      2 = {
                    //          matchInv = null
                    //          matchRev = { chain="He owns a clown", overlap="5", fragId="1" },
                    //          matchCon = { chain="He owns a", overlap="5", fragId="2" },
                    //      },
                    // }
                    for( int j = 0; j < fragList.size(); j++ ){
                        if( j != index ) {

                            matchFrag = fragList.get(j);

                            //
                            newMatch = match(String.valueOf(j), currentFrag, matchFrag);


                            if (newMatch != null) {
                                if ( fragMatchStruct != null ) {

                                    // compare number of match chars
                                    //
                                    if ( fragMatchStruct != null ) tempStruct = (HashMap) fragMatchStruct.get("matchInv");
                                    tempNewMatchInv = (HashMap) newMatch.get("matchInv");

                                    if ( ( tempStruct != null ) || tempNewMatchInv != null && tempStruct != null && (int) tempStruct.get("overlap") < (int) tempNewMatchInv.get("overlap") ) {
                                        fragMatchStruct.put("matchInv", tempNewMatchInv);
                                    }

                                    //
                                    if ( fragMatchStruct != null ) tempStruct = (HashMap) fragMatchStruct.get("matchRev");
                                    tempNewMatchRev = (HashMap) newMatch.get("matchRev");

                                    if ( ( tempStruct != null ) || tempNewMatchRev != null && tempStruct != null && (int) tempStruct.get("overlap") < (int) tempNewMatchRev.get("overlap") ) {
                                        fragMatchStruct.put("matchRev", tempNewMatchRev);
                                    }

                                    //
                                    if ( fragMatchStruct != null ) tempStruct = (HashMap) fragMatchStruct.get("matchCon");
                                    tempNewMatchCon = (HashMap) newMatch.get("matchCon");

                                    if ( ( tempStruct != null ) || tempNewMatchCon != null && tempStruct != null && (int) tempStruct.get("overlap") < (int) tempNewMatchCon.get("overlap") ) {
                                        fragMatchStruct.put("matchCon", tempNewMatchCon);
                                    }

                                    map.replace( index, fragMatchStruct );

                                } else {
                                    map.put( index, newMatch );
                                }
                            }
                        }
                    }

                    // after the first iteration it build a new list of fragment with the best matches, if a match is ambigus
                    // the fragment is proccesed again, and duplicates are deleted
                    // listFrag = ["He owns a clown", "He owns a"]
                    if ( index == fragList.size() - 1 ) {
                        fragmentsSet.clear();

                        int k = 0;
                        HashMap elem = null;
                        HashMap eInv = null, eRev = null, eCon = null;
                        HashMap concatResult = null;

                        Set keys = map.keySet();
                        Iterator itr = keys.iterator();

                        while ( itr.hasNext() ) {

                            elem = map.get( itr.next() );

                            eInv = (HashMap) elem.get( "matchInv" );
                            eRev = (HashMap) elem.get( "matchRev" );
                            eCon = (HashMap) elem.get( "matchCon" );

                            if ( eRev != null && eCon != null && eInv == null ) {

                                if ( eRev.get( "fragId" ) != eCon.get( "fragId" ) ) {
                                    concatResult = concat( (String) eRev.get( "chain" ), (String) eCon.get( "chain" ) );
                                    fragmentsSet.add( (String) concatResult.get( "chain" ) );

                                }else {
                                    fragmentsSet.add( (String) fragList.get( k ) );

                                }

                            } else if ( eInv != null && eCon != null && eRev == null ) {

                                if ( eInv.get( "fragId" ) != eCon.get( "fragId" ) ) {
                                    concatResult = concat( (String) eInv.get( "chain" ), (String) eCon.get( "chain" ) );
                                    fragmentsSet.add( (String) concatResult.get( "chain" ) );

                                }else {
                                    fragmentsSet.add( (String) fragList.get( k ) );

                                }

                            } else if ( eInv != null && eRev != null && eCon == null ) {

                                if ( eInv.get( "fragId" ) != eRev.get( "fragId" ) ) {
                                    concatResult = concat( (String) eInv.get( "chain" ), (String) eRev.get( "chain" ) );
                                    fragmentsSet.add( (String) concatResult.get( "chain" ) );

                                }else {
                                    fragmentsSet.add( (String) fragList.get( k ) );

                                }

                            } else if ( eInv != null ){
                                fragmentsSet.add( (String) eInv.get( "chain" ) );

                            } else if ( eRev != null ){
                                fragmentsSet.add( (String) eRev.get( "chain" ) );

                            } else if (  eCon != null ) {
                                fragmentsSet.add( (String) eCon.get( "chain" ) );

                            }

                            k++;
                        }

                        fragList = new ArrayList( Arrays.asList( fragmentsSet.toArray() ) );


                        map = new HashMap();
                        index = 0;
                    }else{
                        index++;
                    }
                }

                result = fragList.get(0);
            }catch ( Exception e ){
                result = null;
            }finally {
            }

            return result;
        }

        /**
         * Returns a new string which contains the fragments parameter.
         *
         * @param frag1
         * @param frag2
         * @return the string fragment that contains the small fragment or a new string of the concatenated fragments, null in other case.
         */
        private static HashMap match(String fragId, String frag1, String frag2){
            HashMap result = new HashMap();
            HashMap resultContained = null;
            HashMap resultInv = null;
            HashMap resultRev = null;
            String temp = null;
            StringBuilder b_frag_small = null, b_frag_large = null;

            String frag_small = frag1;
            String frag_large = frag2;

            try{

                if ( frag1.length() > frag2.length() ){
                    temp = frag_large;
                    frag_large = frag_small;
                    frag_small = temp;
                }

                if ( frag_large.contains( frag_small ) ){
                    resultContained = new HashMap();
                    resultContained.put( "chain", frag_large );
                    resultContained.put( "overlap", frag_small.length() );
                    resultContained.put( "fragId", fragId );

                } else {

                    b_frag_small = new StringBuilder( frag_small );
                    b_frag_large = new StringBuilder( frag_large );

                    // checks if the large fragment ends with the smaller
                    resultInv = concat( frag_large, frag_small );
                    if( resultInv != null) resultInv.put( "fragId", fragId );

                    // checks if the small fragment ends with the larger
                    resultRev = concat( b_frag_large.reverse().toString(), b_frag_small.reverse().toString() );

                    if( resultRev != null ) {
                        resultRev.replace( "chain", new StringBuilder( resultRev.get( "chain" ).toString() ).reverse().toString() );
                        resultRev.put( "fragId", fragId );
                    }

                }

                if( resultInv != null) result.put( "matchInv", resultInv );
                if( resultRev != null) result.put( "matchRev", resultRev );
                if( resultContained != null) result.put( "matchCon", resultContained );

                if ( result.size() == 0 ) result = null;


            }catch ( Exception e ){
                result = null;
            }

            return result;
        }


        /**
         * Returns a new string concatenated by the match area
         *
         * @param start
         * @param end
         * @return
         */
        private static HashMap concat( String start, String end ) {
            HashMap result = null;
            int indexOcurrence = -1, overlap = -1;
            boolean isSuffix = false;

            try {
                int endOffset = end.length();

                int startOffset = start.length() - end.length();

                while ( endOffset > MIN_CHARS_TO_MATCH ) {
                    --endOffset;
                    startOffset++;

                    isSuffix = start.regionMatches(false, startOffset, end, 0, endOffset);

                    if ( isSuffix ) {
                        // get the index where the chains match
                        indexOcurrence = start.lastIndexOf( end.substring( 0, endOffset ) );
                        overlap = endOffset;
                        // end the while loop
                        endOffset = -1;
                    }
                }

                if ( indexOcurrence > -1 ) {
                    result = new HashMap();
                    result.put( "chain", start.substring( 0, indexOcurrence ).concat( end ) );
                    result.put( "overlap", overlap );
                }

            }catch ( Exception e ){
                result = null;
            }
            return result;
        }
    }
}

