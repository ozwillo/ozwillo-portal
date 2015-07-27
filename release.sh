# args : optional RC number for oasis-portal and oasis-spring-integration (ex. 1, 2 1...)

# generic release function :
release_project() {

# computing release version & RC branch if any :
pushd $MAVEN_ROOT

# getting project version and computing next one :
# see http://stackoverflow.com/questions/3545292/how-to-get-maven-project-version-to-the-bash-command-line
# getting version ex. 1.10-SNAPSHOT :
VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | sed -n -e '/^\[.*\]/ !{ /^[0-9]/ { p; q } }'`
# NB. if this plugin is not yet there, is rather "Downloaded: https://..."
# so it must be retried (but using -o is worse since it prevents downloading)
# Advances the last number of the given version string by one.
function remove_version_suffix () {
    local v=$1
    # Get the last number. First remove any suffixes (such as '-SNAPSHOT').
    local cleaned=`echo $v | sed -e 's/[^0-9][^0-9]*$//'`
    echo $cleaned
}
function advance_version () {
    local v=$1
    # Get the last number. First remove any suffixes (such as '-SNAPSHOT').
    local cleaned=`echo $v | sed -e 's/[^0-9][^0-9]*$//'`
    local last_num=`echo $cleaned | sed -e 's/[0-9]*\.//g'`
    local next_num=$(($last_num+1))
    # Finally replace the last number in version string with the new one.
    echo $v | sed -e "s/[0-9][0-9]*\([^0-9]*\)$/$next_num/"
}
function rewind_version () {
    local v=$1
    # Get the last number. First remove any suffixes (such as '-SNAPSHOT').
    local cleaned=`echo $v | sed -e 's/[^0-9][^0-9]*$//'`
    local last_num=`echo $cleaned | sed -e 's/[0-9]*\.//g'`
    local next_num=$(($last_num-1))
    # Finally replace the last number in version string with the new one.
    echo $v | sed -e "s/[0-9][0-9]*\([^0-9]*\)$/$next_num/"
}

if [ "$RC" != "" ]
then
VERSION_WITHOUT_SUFFIX=$(remove_version_suffix $VERSION)
if [ "$VERSION_WITHOUT_SUFFIX" != "$VERSION" ]
then
# was on next SNAPSHOT, rewind
VERSION_WITHOUT_SUFFIX=$(rewind_version $VERSION_WITHOUT_SUFFIX)
fi
RELEASE_VERSION=$VERSION_WITHOUT_SUFFIX"-RC$RC"
NEXT_RC=$(($RC+1))
NEXT_VERSION=$VERSION_WITHOUT_SUFFIX"-RC$NEXT_RC"-SNAPSHOT
RC_START_TAG="$RELEASE_NAME-$VERSION_WITHOUT_SUFFIX"
BRANCH=$RELEASE_NAME-$VERSION_WITHOUT_SUFFIX"-rc"
if [ "$RC" = "1" ]
then
echo "About to create RC branch $BRANCH on existing release $RC_START_TAG tag.If OK hit enter, else abort (CTRL-C)"
read
git checkout $RC_START_TAG
git branch $BRANCH
git push origin $BRANCH
fi
else
RELEASE_VERSION=$(remove_version_suffix $VERSION)
NEXT_VERSION=$(advance_version $RELEASE_VERSION)-SNAPSHOT
BRANCH=master
fi
TAG="$RELEASE_NAME-$RELEASE_VERSION"
echo "Releasing $RELEASE_NAME (now at $VERSION) as $TAG and bumping to $NEXT_VERSION. If OK hit enter, else abort (CTRL-C)"
read

popd

git pull origin $BRANCH
echo "Pulled git. Abort if any problem (CTRL-C), else hit enter."
read

# dependencies outside this current project :
pushd $MAVEN_ROOT
SNAPSHOT_DEPS=`mvn dependency:list|grep -v $RELEASE_NAME|grep SNAPSHOT`
popd
if [ "$SNAPSHOT_DEPS" != "" ]
then
   echo "ERROR Aborting ! This project has SNAPSHOT dependencies : $SNAPSHOT_DEPS"
   exit 1
fi
echo "WARNING Check that dependencies ($DEPENDENCIES) have been released and checked out with said release and this project ($RELEASE_NAME)'s pom updated to their latest version if they have any change ! Abort if any problem (CTRL-C), else hit enter."
read
echo "WARNING Check that unit tests work (mvn clean install) ! Abort if any problem (CTRL-C), else hit enter."
read

if [ "$MINIFY_COMMAND" != "" ]
then
   # minify
   eval $MINIFY_COMMAND
fi
git status
echo "WARNING Check that everything is committed, especially newly minified changes if any ! Abort if any problem (CTRL-C), else hit enter."
read

# go in maven project to release it :
pushd $MAVEN_ROOT

mvn versions:set -DnewVersion=$RELEASE_VERSION
# ex. 1.10
# NB. can't execute unit tests because they sometimes fail undeterministically (IllegalStateException)
mvn clean install versions:commit -DskipTests=true
echo "Upgraded project to release version. Check that it reflects in the above logs (ex. Installing .../$RELEASE_NAME-...-$RELEASE_VERSION.jar), then hit enter"
read
echo "Committing changed poms :"
find . -name "pom.xml" -exec git add {} \; -print
git commit -m "Tag version $TAG"
echo "WARNING About to tag already upgraded and committed version, please check everything is OK ! If not then abort (CTRL-C), else hit enter"
read
git tag $TAG
mvn versions:set -DnewVersion=$NEXT_VERSION
# ex. 1.11-SNAPSHOT
mvn clean install versions:commit -DskipTests
echo Committing changed poms :
find . -name "pom.xml" -exec git add {} \; -print
git commit -m "Bump to next development iteration"
popd
echo "WARNING About to push tag, if you have a doubt about it abort (CTRL-C) and replace your project's .git folder by a freshly cloned one, else hit enter."
echo "If you aborted by error, you will need to push this changes and created tags manually."
read
git push origin $BRANCH && git push origin $BRANCH --tags

echo "Successfully released $RELEASE_NAME ! Checking it out (required for dependent projects) :"
git checkout $TAG

} # end of release_project()


# RC suffix ex. 1 :
RC=$2
RELEASE_NAME=oasis-spring-integration
NVM_VERSION=v0.10.36
DEPENDENCIES=
MAVEN_ROOT=.
MINIFY_COMMAND=


# dependencies ON $RELEASE_NAME :
pushd portal-parent
SNAPSHOT_DEPS=`mvn dependency:list|grep $RELEASE_NAME|grep SNAPSHOT`
popd
if [ "$SNAPSHOT_DEPS" != "" ]
then
   echo "The main project has SNAPSHOT dependencies : $SNAPSHOT_DEPS, releasing it"

pushd ../oasis-spring-integration
release_project
git status
popd

fi


# RC suffix ex. 1 :
RC=$1
RELEASE_NAME=oasis-portal
NVM_VERSION=v0.10.36
DEPENDENCIES=oasis-spring-integration
MAVEN_ROOT=portal-parent
# minify : source nvm else Unknown command https://github.com/creationix/nvm/issues/521
MINIFY_COMMAND=". ~/.nvm/nvm.sh ; nvm install $NVM_VERSION ; ./jsx.py ; echo Minified."

release_project
