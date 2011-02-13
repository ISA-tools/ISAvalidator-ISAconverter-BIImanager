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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.isatools.isatab.commandline.PermModShellCommand;
import org.isatools.isatab.commandline.UserAddShellCommand;
import org.isatools.isatab.commandline.UserModShellCommand;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabInvalidValueException;
import org.isatools.tablib.exceptions.TabMissingValueException;
import org.isatools.tablib.mapping.properties.DatePropertyMappingHelper;
import uk.ac.ebi.bioinvindex.dao.StudyDAO;
import uk.ac.ebi.bioinvindex.dao.UserDAO;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.VisibilityStatus;
import uk.ac.ebi.bioinvindex.model.security.Person;
import uk.ac.ebi.bioinvindex.model.security.User;
import uk.ac.ebi.bioinvindex.model.security.UserRole;
import uk.ac.ebi.bioinvindex.utils.StringEncryption;
import uk.ac.ebi.utils.regex.RegEx;

import javax.persistence.EntityManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.*;

/**
 * The Permission manager wrapper. This class is mainly used for the Command Lines implementation, but
 * could be useful elsewhere.
 * <p/>
 * <p><b>PLEASE NOTE</b>: This class never commits its operations! This is up to the caller.</p>
 * <p/>
 * <dl><dt>date</dt><dd>May 14, 2010</dd></dl>
 *
 * @author brandizi
 */
public class PermissionManager {
    private final DaoFactory daoFactory;

    /**
     * This is a copy of {@link DatePropertyMappingHelper#VALID_FORMATS}. I need to redefine it here, cause
     * otherwise log4j will be activated too early.
     */
    public final static String[] VALID_DATE_FORMATS = new String[]{"yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy"};


    public PermissionManager(EntityManager entityManager) {
        this.daoFactory = DaoFactory.getInstance(entityManager);
    }

    /**
     * The command line options that are used in the user manipulation commands, {@link UserAddShellCommand}
     * and {@link UserModShellCommand}.
     */
    public static Options createUserDefOptions() {
        return createUserDefOptions(new Options());
    }

    /**
     * A Version of {@link #createUserDefOptions()} that allows to pass pre-set options.
     */
    @SuppressWarnings("static-access")
    public static Options createUserDefOptions(Options baseOptions) {
        baseOptions.addOption(
                OptionBuilder.withArgName("name")
                        .hasArg()
                        .withDescription("Required for a new user")
                        .withLongOpt("name")
                        .create("n")
        );
        baseOptions.addOption(
                OptionBuilder.withArgName("surename")
                        .hasArg()
                        .withDescription("Required for a new user")
                        .withLongOpt("surename")
                        .create("s")
        );
        baseOptions.addOption(
                OptionBuilder.withArgName("password")
                        .hasArg()
                        .withDescription("Required for a new user")
                        .withLongOpt("password")
                        .create("p")
        );
        baseOptions.addOption(
                OptionBuilder.withArgName("affiliation")
                        .hasArg()
                        .withDescription("Required for a new user")
                        .withLongOpt("affiliation")
                        .create("f")
        );
        baseOptions.addOption(
                OptionBuilder.withArgName("address")
                        .hasArg()
                        .withLongOpt("address")
                        .create("a")
        );
        baseOptions.addOption(
                OptionBuilder.withArgName("email")
                        .hasArg()
                        .withDescription("Required for a new user")
                        .withLongOpt("email")
                        .create("e")
        );
        baseOptions.addOption(
                OptionBuilder.withArgName("date")
                        .withDescription("Join date in one of the formats: " + Arrays.toString(VALID_DATE_FORMATS))
                        .hasArg()
                        .withLongOpt("date")
                        .create("d")
        );
        baseOptions.addOption(
                OptionBuilder.withArgName("submitter|curator")
                        .withDescription("User role")
                        .hasArg()
                        .withDescription("Required for a new user")
                        .withLongOpt("role")
                        .create("r")
        );
        return baseOptions;
    }

    /**
     * A Version of {@link #createPermModOptions()} that allows to pass pre-set options.
     */
    @SuppressWarnings("static-access")
    public static Options createPermModOptions(Options baseOptions) {
        baseOptions.addOption(
                OptionBuilder.withArgName("study-acc=[+|-]login1,login2,login2...")
                        .hasArg()
                        .withDescription("+/- to add/remove, nothing to set exactly that list. Option is repeatible")
                        .withLongOpt("study-owners")
                        .create("o")
        );
        baseOptions.addOption(
                OptionBuilder.withArgName("[+|-]study-acc1,acc2,acc3...")
                        .hasArg()
                        .withDescription("+/- to add/remove, nothing to set exactly that list. Option is repeatible")
                        .withLongOpt("private")
                        .create("v")
        );
        baseOptions.addOption(
                OptionBuilder.withArgName("[+|-]study-acc1,acc2,acc3...")
                        .hasArg()
                        .withDescription("+/- to add/remove, nothing to set exactly that list. Option is repeatible")
                        .withLongOpt("public")
                        .create("p")
        );
        return baseOptions;
    }

    /**
     * The command line options used for commands about study permission manipulation, {@link PermModShellCommand}.
     */
    public static Options createPermModOptions() {
        return createPermModOptions(new Options());
    }

    /**
     * Creates a user from the options ({@link #createUserDefOptions()}) passed to the command line.
     */
    public Person createNewUserFromOptions(String login, CommandLine cmdl) {
        String forename = cmdl.getOptionValue("n");
        String surname = cmdl.getOptionValue("s");
        String pwd = cmdl.getOptionValue("p");
        String affiliation = cmdl.getOptionValue("f");
        String addr = cmdl.getOptionValue("a");
        String email = cmdl.getOptionValue("e");
        String dates = cmdl.getOptionValue("d");
        Date date = null;
        if (dates != null) {
            try {
                date = DateUtils.parseDate(dates, VALID_DATE_FORMATS);
            }
            catch (ParseException ex) {
                throw new TabInvalidValueException("Date '" + dates + "' is invalid");
            }
        }

        String role = cmdl.getOptionValue("r");
        if (role != null && !"submitter".equalsIgnoreCase(role) && !"curator".equalsIgnoreCase(role)) {
            throw new TabInvalidValueException("role value: '" + role + "' is invalid");
        }
        Person result = new Person();

        result.setUserName(login);
        result.setFirstName(forename);
        result.setLastName(surname);
        if (pwd != null) {
            result.setPassword(StringEncryption.getInstance().encrypt(pwd));
        }
        result.setAffiliation(affiliation);
        result.setAddress(addr);
        result.setEmail(email);
        result.setJoinDate(date);

        if ("submitter".equalsIgnoreCase(role)) {
            result.setRole(UserRole.SUBMITTER);
        } else if ("curator".equalsIgnoreCase(role)) {
            result.setRole(UserRole.CURATOR);
        }

        return result;
    }

    /**
     * Validate a user before storage.
     */
    public void validateUser(Person user) {
        if (user == null) {
            throw new TabInvalidValueException("Cannot add a null user to the BII db!");
        }

        if (StringUtils.trimToNull(user.getUserName()) == null
                || StringUtils.trimToNull(user.getPassword()) == null
                || StringUtils.trimToNull(user.getFirstName()) == null
                || StringUtils.trimToNull(user.getLastName()) == null
                || StringUtils.trimToNull(user.getAffiliation()) == null
                || StringUtils.trimToNull(user.getEmail()) == null
                || user.getRole() == null) {
            throw new TabMissingValueException("Invalid user, missing required attribute(s)");
        }
    }

    /**
     * Adds a user to the DB.
     */
    public long addUser(Person user) {
        UserDAO dao = daoFactory.getUserDAO();
        User u = dao.getByUsername(user.getUserName());
        if (u != null) {
            throw new TabInvalidValueException("Login '" + user.getUserName() + "' already exists!");
        }

        validateUser(user);
        return dao.save(user);
    }

    /**
     * Updates an existing user in the DB. It merges the new user over the old one. That is: overrides a property
     * of the previous user only if the new corresponding property is non-null.
     */
    public void updateUser(Person user) {
        String login = StringUtils.trimToNull(user.getUserName());
        if (login == null) {
            throw new TabMissingValueException("Cannot update user: user login is null");
        }

        Person userDB = getUserByLogin(login);
        mergeProps(user, userDB, "address", "firstName", "lastName", "affiliation", "password", "email");
        UserRole role = user.getRole();
        if (role != null) {
            userDB.setRole(role);
        }
        Date date = user.getJoinDate();
        if (date != null) {
            userDB.setJoinDate(date);
        }

        validateUser(userDB);
        daoFactory.getUserDAO().update(userDB);
    }

    /**
     * call the getters/setters given by propNames and, when the corresponding property in org is non null, overrides
     * the same propery in dest.
     */
    private void mergeProps(Person org, Person dest, String... propNames) {
        for (String pname : propNames) {
            mergeProp(org, dest, pname);
        }
    }

    /**
     * call the getters/setters given by propNames and, when the corresponding property in org is non null, overrides
     * the same propery in dest.
     */
    private void mergeProp(Person org, Person dest, String propName) {
        try {
            String pval = StringUtils.trimToNull(BeanUtils.getProperty(org, propName));
            if (pval != null) {
                BeanUtils.setProperty(dest, propName, pval);
            }
        }
        catch (Exception e) {
            throw new TabInternalErrorException("Internal error in the permission manager: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a user from the DB.
     *
     * @throws TabMissingValueException if the login doesn't exist.
     */
    public void deleteUser(String login) {
        if (login == null) {
            throw new TabMissingValueException("Cannot update user: user login is null");
        }

        Person userDB = getUserByLogin(login);
        daoFactory.getUserDAO().deleteById(userDB.getId());
    }

    /**
     * Sets the ownership of studies, using the options and syntax passed via command line.
     * See {@link #setStudyOwners(String)}
     */
    public void setStudyOwners(CommandLine cmdl) {
        String specs[] = cmdl.getOptionValues("o");
        if (specs == null) {
            return;
        }
        for (String spec : specs) {
            setStudyOwners(spec);
        }
    }


    /**
     * Sets a single study ownership, using the command line syntax.
     *
     * @param spec a string in the format: studyAcc=[+|-]login1,login2,... if +|- is specified the users will
     *             be added to / removed from the study, otherwise this will be the exact list of study's users
     */
    public void setStudyOwners(String spec) {
        spec = StringUtils.trimToNull(spec);
        if (spec == null) {
            throw new TabInvalidValueException("Invalid syntax for the study/user list: '" + spec + "'");
        }

        RegEx re = new RegEx("([^=]+)\\=(\\+|\\-)?(.*)");
        String[] parts = re.groups(spec);
        if (parts == null || parts.length != 4) {
            throw new TabInvalidValueException("Invalid syntax for the study/user list: '" + spec + "'");
        }

        String studyAcc = parts[1], op = parts[2], loginStr = parts[3];
        String logins[] = loginStr.split("\\,");
        setStudyOwners(studyAcc, op, logins);
    }

    /**
     * Sets the ownership of a single study.
     * <p/>
     * operation can be null (sets exactly the parameter list), "+" (adds the logins) or "-" (remove the logins).
     */
    public void setStudyOwners(String studyAcc, String operation, String... logins) {
        operation = StringUtils.trimToNull(operation);
        if (operation != null && !"+".equals(operation) && !"-".equals(operation)) {
            throw new TabInvalidValueException("Permission manager, invalid syntax for '" + operation + "'");
        }

        Study study = getStudyByAcc(studyAcc);
        if (operation == null) {
            Collection<User> users = new HashSet<User>();
            for (String login : logins) {
                users.add(getUserByLogin(login));
            }
            study.setUsers(users);
        } else {
            for (String login : logins) {
                if ("+".equals(operation)) {
                    if (searchStudyUser(study, login) == null) {
                        Person person = getUserByLogin(login);
                        study.addUser(person);
                    }
                } else {
                    User u = searchStudyUser(study, login);
                    if (u != null) {
                        study.removerUser(u);
                    }
                }
            }
        }
        daoFactory.getStudyDAO().update(study);
    }


    /**
     * Search a user in a study, returns non null if found.
     * <p/>
     * TODO: This is needed, cause the equivalence methods in the {@link User} class don't work
     */
    private User searchStudyUser(Study study, String login) {
        for (User u : study.getUsers()) {
            if (login.equals(u.getUserName())) {
                return u;
            }
        }
        return null;
    }

//	public void setUserStudies ( String spec )
//	{
//		
//	}
//
//	public void setUserStudies ( String login, String operation, String... studyAccs )
//	{
//		operation = StringUtils.trimToNull ( operation  );
//		if ( operation != null && !"+".equals ( operation ) && !"-".equals ( operation ) )
//			throw new TabInvalidValueException ( "Permission manager, invalid syntax for '" + operation + "'" );
//
//		if ( login == null )
//			throw new TabMissingValueException ( "Cannot change user studies: user login is null" );
//
//		Person user = getUserByLogin ( login );
//		StudyDAO dao = daoFactory.getStudyDAO ();
//		
//		Set<String> studyAccSet = new HashSet<String> ();
//		studyAccSet.addAll ( Arrays.asList ( studyAccs ) );
//
//		if ( operation == null )
//		{
//			for ( Study study: getStudiesOfUser ( user ) ) 
//			{
//				if ( !studyAccSet.contains ( study.getAcc () ) ) {
//					study.removerUser ( user );
//					dao.update ( study );
//				}
//			}
//			
//			for ( String studyAcc: studyAccSet ) 
//			{
//				Study study = getStudyByAcc ( studyAcc );
//				study.addUser ( user );
//				dao.update ( study );
//			}
//		}
//		else
//		{
//			for ( String studyAcc: studyAccSet ) 
//			{
//				Study study = getStudyByAcc ( studyAcc );
//				if ( "+".equals ( operation ))
//					study.addUser ( user );
//				else 
//					study.removerUser ( user );
//				dao.update ( study );
//			}
//		}
//	}

    /**
     * Set the status of a study set, getting the list of studies from the command line arguments.
     */
    public void setStatus(CommandLine cmdl, VisibilityStatus status) {
        String specs[] = cmdl.getOptionValues(status == VisibilityStatus.PRIVATE ? "v" : "p");
        if (specs == null) {
            return;
        }
        for (String spec : specs) {
            if (status == VisibilityStatus.PRIVATE) {
                setPrivate(spec);
            } else {
                setPublic(spec);
            }
        }
    }

    /**
     * Set the status of a study set, getting the list of studies from the command line arguments.
     */
    public void setVisibility(CommandLine cmdl) {
        setStatus(cmdl, VisibilityStatus.PRIVATE);
        setStatus(cmdl, VisibilityStatus.PUBLIC);
    }

    /**
     * Set the status of a study set as private, getting the list of studies from the "--private" command line argument.
     */
    public void setPrivate(String spec) {
        spec = StringUtils.trimToNull(spec);
        if (spec == null) {
            throw new TabInvalidValueException("Invalid syntax for the study list: '" + spec + "'");
        }
        String accs[] = spec.split("\\,");
        if (accs == null || accs.length == 0) {
            throw new TabInvalidValueException("Invalid syntax for the study list: '" + spec + "'");
        }
        setPrivate(accs);
    }

    /**
     * Set the status of a study set
     */
    public void setPrivate(String... studyAccs) {
        setStatus(VisibilityStatus.PRIVATE, studyAccs);
    }

    /**
     * Set the status of a study set
     */
    public void setStatus(VisibilityStatus status, String... studyAccs) {
        Set<String> studyAccSet = new HashSet<String>();
        studyAccSet.addAll(Arrays.asList(studyAccs));
        StudyDAO dao = daoFactory.getStudyDAO();

        for (String acc : studyAccSet) {
            // System.out.println ( "Setting " + status + " for " + acc );
            Study study = getStudyByAcc(acc);
            study.setStatus(status);
            dao.update(study);
        }

    }

    /**
     * Set the status of a study set as private, getting the list of studies from the "--public" command line argument.
     */
    public void setPublic(String spec) {
        spec = StringUtils.trimToNull(spec);
        if (spec == null) {
            throw new TabInvalidValueException("Invalid syntax for the study list: '" + spec + "'");
        }
        String accs[] = spec.split("\\,");
        if (accs == null || accs.length == 0) {
            throw new TabInvalidValueException("Invalid syntax for the study list: '" + spec + "'");
        }
        setPublic(accs);
    }

    /**
     * Set the status of a study set
     */
    public void setPublic(String... studyAccs) {
        setStatus(VisibilityStatus.PUBLIC, studyAccs);
    }

    /**
     * Gets all the studies in the DB
     */
    @SuppressWarnings("unchecked")
    public List<Study> getStudies() {
        Session session = (Session) daoFactory.getEntityManager().getDelegate();
        Criteria criteria = session.createCriteria(Study.class).addOrder(Order.asc("acc"));
        return criteria.list();
//		
//		
//		
//		return daoFactory.getEntityManager ().createQuery ( 
//			"SELECT distinct s FROM Study s ORDER BY s.acc" 
//		)
//		.getResultList();
    }

    /**
     * Gets all the users in the DB.
     */
    @SuppressWarnings("unchecked")
    public List<Person> getUsers() {
        return daoFactory.getEntityManager().createQuery(
                "SELECT distinct p FROM Person p ORDER BY p.userName"
        )
                .getResultList();
    }

    /**
     * Finds a user by its login.
     *
     * @throws TabInvalidValueException if not found or it is not a {@link Person}.
     */
    public Person getUserByLogin(String login) {
        User u = daoFactory.getUserDAO().getByUsername(login);
        if (u == null) {
            throw new TabInvalidValueException("user '" + login + "' not found");
        }
        if (!(u instanceof Person)) {
            throw new TabInvalidValueException("user '" + login + "' is not valid (not a Person)");
        }
        return (Person) u;
    }

    /**
     * Finds a study by accession.
     *
     * @throws TabInvalidValueException if not found.
     */
    public Study getStudyByAcc(String acc) {
        Study s = daoFactory.getStudyDAO().getByAcc(acc);
        if (s == null) {
            throw new TabInvalidValueException("study '" + acc + "' not found");
        }
        return s;
    }

//	public List<Study> getStudiesOfUser ( User user )
//	{
//		return daoFactory.getEntityManager ().createQuery ( 
//				"SELECT distinct s FROM Study s JOIN s.users u WHERE u.userName = :uacc" )
//				.setParameter ( "uacc", user.getUserName () )
//				.getResultList();		
//	}

    /**
     * Pretty printing of a user, for command line printing.
     */
    public static String formatUser(Person user) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        out.println("LOGIN: " + user.getUserName() + ", Password: " + user.getPassword());
        out.println("  Name/Email: " + user.getFirstName() + " " + user.getLastName() + " <" + user.getEmail() + ">");
        out.println("  Affiliation: " + user.getAffiliation());
        out.println("  Address: " + user.getAddress());
        out.println("  Join Date: " + user.getJoinDate());
        out.println("  Role: " + user.getRole());
        out.println("  DB's ID: " + user.getId());
        out.println();
        out.flush();
        return sw.toString();
    }

    /**
     * Pretty printing of a study, for command line printing.
     */
    public static String formatStudy(Study study) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        out.println("ACCESSION: " + study.getAcc());
        out.printf("  Title: %50.50s\n", study.getTitle());
        out.println("  Submission Date: " + study.getSubmissionDate() + ", Release Date: " + study.getReleaseDate());
        out.println("  Visibility: " + study.getStatus());
        out.println("  Users:");
        out.print("  ");
        for (User u : study.getUsers()) {
            out.printf("%15.15s  ", u.getUserName());
        }
        out.println();
        out.println();
        out.flush();
        return sw.toString();
    }

}
