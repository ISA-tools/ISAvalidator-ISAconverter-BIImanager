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

package org.isatools.gui.datamanager.studyaccess;

import org.isatools.isatab.commandline.AbstractImportLayerShellCommand;
import uk.ac.ebi.bioinvindex.dao.UserDAO;
import uk.ac.ebi.bioinvindex.dao.UserManagementService;
import uk.ac.ebi.bioinvindex.dao.UserManagementServiceImpl;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;
import uk.ac.ebi.bioinvindex.model.VisibilityStatus;
import uk.ac.ebi.bioinvindex.model.security.Person;
import uk.ac.ebi.bioinvindex.model.security.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.*;

/**
 * UserManagementControl
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Nov 24, 2009
 */


public class UserManagementControl {

    private EntityManager entityManager;
    private UserManagementService userManagement;
    private List<User> availableUsers;

    public UserManagementControl() {
        createEntityManager();
    }

    /**
     * This method is now always invoked before processing the permission change form. We recreate the manager because
     * otherwise bad things happen, such as objects from the study persistence that are believed to be commited (they
     * were in another entity manager, but appear here). In order to keep this class consistent, this method also
     * invokes {@link #createUserManagement()} and {@link #getUsers()} (which caches the users).
     */
    public void createEntityManager() {
        Properties hibProps = AbstractImportLayerShellCommand.getHibernateProperties();
        hibProps.setProperty("hibernate.search.indexing_strategy", "manual");

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("BIIEntityManager", hibProps);
        this.entityManager = entityManagerFactory.createEntityManager();

        createUserManagement();
        getUsers();
    }

    private void createUserManagement() {
        userManagement = new UserManagementServiceImpl(entityManager);
    }

    public List<User> getUsers() {
        availableUsers = userManagement.getAllUsers();
        return availableUsers;
    }

    public void addUserToStudy(String studyAcc, User user) throws Exception {
        userManagement.addUserToStudy(studyAcc, user);
    }

    public void removeUserFromStudy(String studyAcc, User user) throws Exception {
        userManagement.removeUserFromStudy(studyAcc, user);
    }

    public void changeStudyVisability(String studyId, VisibilityStatus status) throws Exception {
        userManagement.changeStudyStatus(studyId, status);
    }

    public Map<String, VisibilityStatus> getAllStudyVisibilityStatus() throws Exception {
        return userManagement.getVisibilityStatusForStudies();
    }

    public String[] getUsernames() throws Exception {
        if (availableUsers == null) {
            availableUsers = getUsers();
        }

        return extractUserNamesFromUserList(availableUsers);
    }

    public static String[] extractUserNamesFromUserList(List<User> users) {
        String[] usernames = new String[users.size()];

        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            usernames[i] = u.getUserName();
        }

        return usernames;
    }

    public User getUserByUsername(String userName) {
        if (availableUsers != null) {
            for (User u : availableUsers) {
                if (u.getUserName().equals(userName)) {
                    return u;
                }
            }
        }
        return null;
    }

    public Set<String> getAllStudies() throws Exception {
        return userManagement.getVisibilityStatusForStudies().keySet();
    }

    public Map<String, Set<String>> getStudiesAndAssociatedUsers() throws Exception {
        return convertUserToUsernames(userManagement.getUsersAssignedToStudies());
    }

    /**
     * Convert a Map containing Study accession to User objects to a Map Containing a mapping
     * between a Study accession to a Set of usernames as Strings.
     *
     * @param studyToUser - Mapping contain Study access to associated users.
     * @return Map<StudyAccessions as String, Set of usernames as String>
     */
    private Map<String, Set<String>> convertUserToUsernames(Map<String, List<User>> studyToUser) throws Exception {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();

        for (String studyAcc : studyToUser.keySet()) {
            result.put(studyAcc, new HashSet<String>());

            for (User u : studyToUser.get(studyAcc)) {
                result.get(studyAcc).add(u.getUserName());
            }
        }

        return result;
    }

    public boolean validateLogin(String userName, String password) throws Exception {
        return userManagement.validateCuratorLogin(userName, password);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void createUser(Person user) throws Exception {
        DaoFactory daoFactory = DaoFactory.getInstance(entityManager);
        UserDAO userDAO = daoFactory.getUserDAO();
        EntityTransaction tns = entityManager.getTransaction();
        tns.begin();
        Long id = userDAO.save(user);
        tns.commit();

        System.out.println("User saved, id #" + id);
    }
}
