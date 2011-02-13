/**

 The ISAconverter, ISAvalidator & BII Management Tool are components of the ISA software suite (http://www.isa-tools.org)

 Exhibit A
 The ISAconverter, ISAvalidator & BII Management Tool are licensed under the Mozilla Public License (MPL) version
 1.1/GPL version 2.0/LGPL version 2.1

 "The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"). You may not use this file except in compliance with the License.
 You may obtain copies of the Licenses at http://www.mozilla.org/MPL/MPL-1.1.html.

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is the ISAconverter, ISAvalidator & BII Management Tool.

 The Initial Developer of the Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
 Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
 http://www.isa-tools.org). All portions of the code written by the ISA Team are Copyright (c)
 2007-2011 ISA Team. All Rights Reserved.

 Contributor(s):
 Rocca-Serra P, Brandizi M, Maguire E, Sklyar N, Taylor C, Begley K, Field D,
 Harris S, Hide W, Hofmann O, Neumann S, Sterk P, Tong W, Sansone SA. ISA software suite:
 supporting standards-compliant experimental annotation and enabling curation at the community level.
 Bioinformatics 2010;26(18):2354-6.

 Alternatively, the contents of this file may be used under the terms of either the GNU General
 Public License Version 2 or later (the "GPL") - http://www.gnu.org/licenses/gpl-2.0.html, or
 the GNU Lesser General Public License Version 2.1 or later (the "LGPL") -
 http://www.gnu.org/licenses/lgpl-2.1.html, in which case the provisions of the GPL
 or the LGPL are applicable instead of those above. If you wish to allow use of your version
 of this file only under the terms of either the GPL or the LGPL, and not to allow others to
 use your version of this file under the terms of the MPL, indicate your decision by deleting
 the provisions above and replace them with the notice and other provisions required by the
 GPL or the LGPL. If you do not delete the provisions above, a recipient may use your version
 of this file under the terms of any one of the MPL, the GPL or the LGPL.

 Sponsors:
 The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project
 (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC
 (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).

 */

package org.isatools.isatab.commandline.permission_manager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.dbunit.operation.DatabaseOperation;
import org.isatools.tablib.exceptions.TabInvalidValueException;
import org.isatools.tablib.exceptions.TabMissingValueException;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.VisibilityStatus;
import uk.ac.ebi.bioinvindex.model.security.Person;
import uk.ac.ebi.bioinvindex.model.security.UserRole;
import uk.ac.ebi.bioinvindex.persistence.StudyPersister;
import uk.ac.ebi.bioinvindex.persistence.pipeline.AssayMaterialPersister;
import uk.ac.ebi.bioinvindex.utils.StringEncryption;
import uk.ac.ebi.bioinvindex.utils.test.TransactionalDBUnitEJB3DAOTest;
import uk.ac.ebi.bioinvindex.utils.testmodels.SimplePipelineModel;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static java.lang.System.out;
import static org.junit.Assert.*;

public class PermissionManagerTest extends TransactionalDBUnitEJB3DAOTest {

    public PermissionManagerTest() throws Exception {
        super();
    }

    @Override
    protected void prepareSettings() {
        beforeTestOperations.add(DatabaseOperation.CLEAN_INSERT);
        dataSetLocation = null;
    }

    // @Ignore
    @Test
    public void testCreateNewUserFromOptions() throws ParseException {
        PermissionManager permMgr = new PermissionManager(entityManager);
        String[] args = new String[]{
                "--name", "Marco", "--surename", "Brandizi", "-p", "secret", "--date", "10/09/1973", "-r", "submitter"
        };
        CommandLineParser clparser = new GnuParser();
        CommandLine cmdl = clparser.parse(permMgr.createUserDefOptions(), args);
        Person user = permMgr.createNewUserFromOptions("zakmck", cmdl);
        out.println(user);
        assertEquals("Wrong created user!", "zakmck", user.getUserName());
        assertEquals("Wrong created user!", "Marco", user.getFirstName());
        assertEquals("Wrong created user!", StringEncryption.getInstance().encrypt("secret"), user.getPassword());
        assertEquals("Wrong created user!", UserRole.SUBMITTER, user.getRole());
        assertEquals("Wrong created user!", new GregorianCalendar(1973, Calendar.SEPTEMBER, 10).getTime(), user.getJoinDate());
    }

    private Person createTestUser(Person user) {
        out.println(user);
        PermissionManager permMgr = new PermissionManager(entityManager);
        permMgr.addUser(user);

        commitTansaction();
        session.flush();

        assertNotNull("User not saved!", user.getId());

        return user;
    }

    private Person createTestUser() {
        return createTestUser(
                new Person("zakmck", "secret", "Marco", "Brandizi", "email", "EBI", "Hinxton, UK", null, UserRole.CURATOR)
        );
    }

    // @Ignore
    @Test
    public void testValidateUserValid() throws ParseException {
        Person user = createTestUser();
        out.println(user);
        PermissionManager permMgr = new PermissionManager(entityManager);
        permMgr.validateUser(user);
    }

    // @Ignore
    @Test
    public void testValidateUserInvalid() throws ParseException {
        Person user = new Person(null, null, "Marco", "Brandizi", "email", "EBI", "Hinxton, UK", null, UserRole.CURATOR);
        out.println(user);
        PermissionManager permMgr = new PermissionManager(entityManager);

        try {
            permMgr.validateUser(user);
        }
        catch (TabMissingValueException e) {
            return;
        }
        fail("User validation of invalid user passed!");
    }

    // @Ignore
    @Test
    public void testValidateUserInvalidRole() throws ParseException {
        PermissionManager permMgr = new PermissionManager(entityManager);
        Person user = new Person("zakmck", "secret", "Marco", "Brandizi", "email", "EBI", "Hinxton, UK", null, null);
        out.println(user);

        try {
            permMgr.validateUser(user);
        }
        catch (TabMissingValueException e) {
            return;
        }
        fail("User validation of invalid user passed!");
    }

    // @Ignore
    @Test
    public void testAddUser() {
        Person user = createTestUser();
        PermissionManager permMgr = new PermissionManager(entityManager);

        Person userdb = permMgr.getUserByLogin("zakmck");

        assertNotNull("Saved user not fetched!", userdb);
        assertEquals("Wrong saved user!", user.getUserName(), userdb.getUserName());
        assertEquals("Wrong saved user!", user.getFirstName(), userdb.getFirstName());
        assertEquals("Wrong saved user!", user.getJoinDate(), userdb.getJoinDate());
        assertEquals("Wrong saved user!", user.getRole(), userdb.getRole());
    }

    // @Ignore
    @Test
    public void testAddExistingUser() {
        Person user = createTestUser();
        session.clear();

        user.setId(null);
        try {
            createTestUser(user);
        }
        catch (TabInvalidValueException e) {
            return;
        }
        fail("Duped login added!");
    }


    // @Ignore
    @Test
    public void testUpdateUser() {
        Person user = createTestUser();

        user.setPassword("secret1");
        user.setRole(UserRole.SUBMITTER);
        user.setJoinDate(new GregorianCalendar(1973, Calendar.SEPTEMBER, 10).getTime());

        PermissionManager permMgr = new PermissionManager(entityManager);
        permMgr.updateUser(user);
        commitTansaction();
        session.flush();

        Person userdb = permMgr.getUserByLogin(user.getUserName());

        assertEquals("Wrong saved user!", user.getUserName(), userdb.getUserName());
        assertEquals("Wrong saved user!", user.getFirstName(), userdb.getFirstName());
        assertEquals("Wrong saved user!", user.getJoinDate(), userdb.getJoinDate());
        assertEquals("Wrong saved user!", user.getRole(), userdb.getRole());
    }

    // @Ignore
    @Test
    public void testDeleteUser() {
        Person user = createTestUser();
        PermissionManager permMgr = new PermissionManager(entityManager);
        permMgr.deleteUser(user.getUserName());
        commitTansaction();
        session.flush();

        try {
            permMgr.getUserByLogin(user.getUserName());
        }
        catch (TabInvalidValueException e) {
            return;
        }

        fail("User deletion failed!");
    }

    // @Ignore
    @Test
    public void testSetStudyOwners() {
        Person user = createTestUser();
        session.clear();

        user.setId(null);
        user.setUserName("zakmck1");
        createTestUser(user);
        session.clear();

        user.setId(null);
        user.setUserName("zakmck2");
        createTestUser(user);
        session.clear();

        SimplePipelineModel pip = new SimplePipelineModel();
        pip.study.setAcc("s1");

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        StudyPersister studyPersister = new StudyPersister(daoFactory, ts);
        AssayMaterialPersister assayMaterialPersister = new AssayMaterialPersister(daoFactory, ts);
        studyPersister.persist(pip.study);
        assayMaterialPersister.persist(pip.assayMaterial1);
        commitTansaction();
        session.flush();


        PermissionManager permMgr = new PermissionManager(entityManager);

        permMgr.setStudyOwners("s1=zakmck,zakmck1");
        commitTansaction();
        session.flush();

        user.setId(null);

        user.setUserName("zakmck");
        assertTrue("setStudyOwners() failed!", pip.study.getUsers().contains(user));

        user.setUserName("zakmck1");
        assertTrue("setStudyOwners() failed!", pip.study.getUsers().contains(user));

        permMgr.setStudyOwners("s1=+zakmck2");
        commitTansaction();
        session.flush();

        user.setUserName("zakmck2");
        assertTrue("setStudyOwners() failed!", pip.study.getUsers().contains(user));

        permMgr.setStudyOwners("s1=-zakmck,zakmck1,zakmck");
        commitTansaction();
        session.flush();

        user.setUserName("zakmck");
        assertFalse("setStudyOwners() failed!", pip.study.getUsers().contains(user));

        user.setUserName("zakmck1");
        assertFalse("setStudyOwners() failed!", pip.study.getUsers().contains(user));

        user.setUserName("zakmck2");
        assertTrue("setStudyOwners() failed!", pip.study.getUsers().contains(user));

        permMgr.setStudyOwners("s1=zakmck");
        commitTansaction();
        session.flush();

        user.setUserName("zakmck");
        assertTrue("setStudyOwners() failed!", pip.study.getUsers().contains(user));

        user.setUserName("zakmck1");
        assertFalse("setStudyOwners() failed!", pip.study.getUsers().contains(user));
    }

    // @Ignore
    @Test
    public void testVisibility() {
        SimplePipelineModel pip = new SimplePipelineModel();
        pip.study.setAcc("s1");

        SimplePipelineModel pip1 = new SimplePipelineModel();
        pip1.study.setAcc("s2");
        pip1.assayMaterial1.setAcc("s2.as1");
        pip1.nas1.setAcc("s2.nas1");

        pip1.nsrc1.setAcc("s2.nsrc1");
        pip1.src1.setAcc("s2.src1");
        pip1.nsrc2.setAcc("s2.nsrc2");
        pip1.src2.setAcc("s2.src2");

        pip1.p1.setAcc("s2.papp1");
        pip1.p2.setAcc("s2.papp2");

        pip1.nar1.setAcc("s2.nar1");
        pip1.nar2.setAcc("s2.nar2");

        pip1.dt1.setAcc("s2.dt1");
        pip1.dt2.setAcc("s2.dt2");

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        StudyPersister studyPersister = new StudyPersister(daoFactory, ts);
        AssayMaterialPersister assayMaterialPersister = new AssayMaterialPersister(daoFactory, ts);
        studyPersister.persist(pip.study);
        assayMaterialPersister.persist(pip.assayMaterial1);
        studyPersister.persist(pip1.study);
        assayMaterialPersister.persist(pip1.assayMaterial1);
        commitTansaction();
        session.flush();

        PermissionManager permMgr = new PermissionManager(entityManager);
        // TODO: add another study!
        permMgr.setPublic("s1,s2");
        commitTansaction();
        session.flush();

        assertEquals("Visibility set failed!", VisibilityStatus.PUBLIC, pip.study.getStatus());
        assertEquals("Visibility set failed!", VisibilityStatus.PUBLIC, pip1.study.getStatus());

    }

    // @Ignore
    @Test
    public void testGetUsers() {
        Person user = createTestUser();
        session.clear();

        user.setId(null);
        user.setUserName("zakmck1");
        createTestUser(user);
        session.clear();

        user.setId(null);
        user.setUserName("zakmck2");
        createTestUser(user);
        session.clear();

        PermissionManager permMgr = new PermissionManager(entityManager);
        List<Person> prs = permMgr.getUsers();
        assertNotNull("getUsers() failed!", prs);
        for (Person p : prs) {
            out.println(p);
        }

        assertEquals("getUsers() failed!", 3, prs.size());

        user.setId(null);
        user.setUserName("zakmck");
        assertEquals("getUsers() failed!", user, prs.get(0));

        user.setUserName("zakmck1");
        assertEquals("getUsers() failed!", user, prs.get(1));

        user.setUserName("zakmck2");
        assertEquals("getUsers() failed!", user, prs.get(2));
    }


    @Test
    public void testGetStudies() {
        SimplePipelineModel pip = new SimplePipelineModel();
        pip.study.setAcc("s1");

        SimplePipelineModel pip1 = new SimplePipelineModel();
        pip1.study.setAcc("s2");
        pip1.assayMaterial1.setAcc("s2.as1");
        pip1.nas1.setAcc("s2.nas1");

        pip1.nsrc1.setAcc("s2.nsrc1");
        pip1.src1.setAcc("s2.src1");
        pip1.nsrc2.setAcc("s2.nsrc2");
        pip1.src2.setAcc("s2.src2");

        pip1.p1.setAcc("s2.papp1");
        pip1.p2.setAcc("s2.papp2");

        pip1.nar1.setAcc("s2.nar1");
        pip1.nar2.setAcc("s2.nar2");

        pip1.dt1.setAcc("s2.dt1");
        pip1.dt2.setAcc("s2.dt2");

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        StudyPersister studyPersister = new StudyPersister(daoFactory, ts);
        AssayMaterialPersister assayMaterialPersister = new AssayMaterialPersister(daoFactory, ts);
        studyPersister.persist(pip.study);
        assayMaterialPersister.persist(pip.assayMaterial1);
        studyPersister.persist(pip1.study);
        assayMaterialPersister.persist(pip1.assayMaterial1);
        commitTansaction();
        session.flush();

        PermissionManager permMgr = new PermissionManager(entityManager);

        List<Study> studies = permMgr.getStudies();
        assertNotNull("getStudies() failed!", studies);
        for (Study s : studies) {
            out.println(s.getAcc());
        }

        assertEquals("getStudies() failed!", 2, studies.size());
        assertTrue("getStudies() failed!", studies.contains(pip.study));
        assertTrue("getStudies() failed!", studies.contains(pip1.study));
    }
}
