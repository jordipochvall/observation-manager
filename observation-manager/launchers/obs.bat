echo Starting ObservationManager

:: Going to batch files dir
cd %0\..\

:. Starting up Observation Manager
start javaw -Duser.timezone=UTC -Dextensions.dir="$DIR"/extensions -cp classpath:classpath/observation-manager-jar-with-dependencies.jar de.lehmannet.om.ObservationManagerApp %1 %2 %3 %4 %5 %6 %7 %8 %9
