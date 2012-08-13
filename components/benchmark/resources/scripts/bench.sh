#! /bin/bash/                                                                                                                                                                      

JAR=/home/jueumb/lidaq_bench.jar
QUERIES=/webstar1/data/swj-bench/queries-200/
OUT=/webstar1/data/swj-bench/queries-200.out/

cd $QUERIES
for q in `ls -1`;
do
    echo "-> Benchmarking query type $q"
    cd $qt
    
    BASE=$OUT/$qt.1base
    SMART=$OUT/$qt.2smart
    SEEALSO=$OUT/$qt.3seealso
    SAMEAS=$OUT/$qt.5sameas
    RDFS=$OUT/$qt.4rdfs
    RDFS_C=$OUT/$qt.7rdfsc
    RDFS_D=$OUT/$qt.6rdfsd
    ALL=$OUT/$qt.8all
    ALL_DIR=$OUT/$qt.9alldir
    ALL_CL=$OUT/$qt.10cl
        
    mkdir $BASE
    mkdir $SMART
    mkdir $RDFS
    mkdir $SEEALSO
    mkdir $SAMEAS
    mkdir $RDFS_C
    mkdir $RDFS_D
    mkdir $ALL
    mkdir $ALL_DIR
    mkdir $ALL_CL
    
    BASE_LOG=$BASE/log
    SMART_LOG=$SMART/log
    RDFS_LOG=$RDFS/log
    SEEALSO_LOG=$SEEALSO/log
    SAMEAS_LOG=$SAMEAS/log
    RDFS_C_LOG=$RDFS_C/log
    RDFS_D_LOG=$RDFS_D/log
    ALL_LOG=$ALL/log
    ALL_DIR_LOG=$ALL_DIR/log
    ALL_CL_LOG=$ALL_CL/log

    mkdir BASE_LOG
    mkdir SMART_LOG
    mkdir $RDFS_LOG
    mkdir $SEEALSO_LOG
    mkdir $SAMEAS_LOG
    mkdir $RDFS_C_LOG
    mkdir $RDFS_D_LOG
    mkdir $ALL_LOG
    mkdir $ALL_DIR_LOG
    mkdir $ALL_CL_LOG
    
    # for q in `ls -1`
    # do
    #     echo -n "|-> $q |"

        java -Xmx3G -jar $JAR BSuite -bd $BASE     -q $q -r OFF -sl ALL 1>$BASE_LOG/$q.out 2>$BASE_LOG/$q.err
        echo -n "#"
        java -Xmx3G -jar $JAR BSuite -bd $SMART    -q $q -r OFF -sl smart 1>$SMART/$q.out 2>$SMART/$q.err
        echo -n "#"
        java -Xmx3G -jar $JAR BSuite -bd $SEEALSO  -q $q -r OFF -sl smart -sA 1>$SEEALSO/$q.out 2>$SEEALSO/$q.err
        echo -n "#"
        java -Xmx3G -jar $JAR BSuite -bd $RDFS     -q $q -r RDFS -sl smart 1>$RDFS/$q.out 2>$RDFS/$q.err
        echo -n "#"
        java -Xmx3G -jar $JAR BSuite -bd $SAMEAS   -q $q -r OWL -sl smart 1>$SAMEAS/$q.out 2>$SAMEAS/$q.err
        echo -n "#"
        java -Xmx3G -jar $JAR BSuite -bd $RDFS_C   -q $q -r RDFS_DYN_CLOSURE -sl smart 1>$RDFS_C/$q.out 2>$RDFS_C/$q.err
        echo -n "#"
        java -Xmx3G -jar $JAR BSuite -bd $RDFS_D   -q $q -r RDFS_DYN_DIR -sl smart 1>$RDFS_D/$q.out 2>$RDFS_D/$q.err
        echo -n "#"
        java -Xmx3G -jar $JAR BSuite -bd $ALL      -q $q -r ALL -sl smart 1>$ALL/$q.out 2>$ALL/$q.err
        echo -n "#"
        java -Xmx3G -jar $JAR BSuite -bd $ALL_DIR  -q $q -r ALL_DYN_DIR -sl smart 1>$ALL_DIR/$q.out 2>$ALL_DIR/$q.err
        echo -n "#"
        java -Xmx3G -jar $JAR BSuite -bd $ALL_CL   -q $q -r ALL_DYN_CLOSURE -sl smart -sA 1>$ALL_CL/$q.out 2>$ALL_CL/$q.err
        echo "#|"

    # done
    # cd ..
done

