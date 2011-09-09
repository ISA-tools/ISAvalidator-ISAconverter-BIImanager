# ISAvalidator, ISAconverter and BII Data Management tool

<p align="center">
<img src="http://isatools.files.wordpress.com/2011/09/isavaletc.png" align="center" alt="ISAcreator"/>
</p>

- General info: <http://isa-tools.org>
- Issue tracking and bug reporting: <https://github.com/ISA-tools/ISAvalidator-ISAconverter-BIImanager/issues>
- Mainline source code: <https://github.com/ISA-tools/ISAvalidator-ISAconverter-BIImanager>
- Twitter: [@isatools](http://twitter.com/isatools)
- IRC: [irc://irc.freenode.net/](irc://irc.freenode.net/ - group is isatab)
- Development blog: <http://isatools.wordpress.com>

## Development

**Get the source:**

You should ***fork*** the ISAvalidator, ISAconverter and BII Data Management tool project to your own GitHub "repository". Then clone this forked repository and start developing. When you make changes to the ISAvalidator, ISAconverter and BII Data Management tool code, we can see the changes you made, review the code and merge with the main repository code base.

**Build dependencies:**
Dependencies are managed by Apache Maven. Please use version 2.1. 

You also need to clone and install the graph2tab module available here <https://github.com/ISA-tools/graph2tab>. Follow these commands to do so:
'git clone git@github.com:ISA-tools/graph2tab.git'
'cd graph2tab'
'mvn clean install' <- this will compile and install the graph2tab module to you local maven repository so that it is made available to the ISAvalidator, ISAconverter and BII Data Management tool package.
    


### Refreshing your clone

A simple `git pull` will suffice!

### Contributing

The main ISAvalidator-ISAconverter-BIImanager source tree is hosted on git (a popular [DVCS](http://en.wikipedia.org/wiki/Distributed_revision_control)), thus you should create a fork of the repository in which you perform development. See <http://help.github.com/forking/>.

We prefer that you send a [*pull request* here on GitHub](http://help.github.com/pull-requests/) which will then be merged into the official main line repository. You need to sign the ISAtools CLA to be able to contribute (see below).

#### Contributor License Agreement

Before we can accept any contributions to the ISAvalidator, ISAconverter or BII Data Manager codebase, you need to sign a [CLA](http://en.wikipedia.org/wiki/Contributor_License_Agreement):

Please email us <isatools@googlegroups.com> to receive the CLA. Then you should sign this and send it back asap so we can add you to our development pool.

> The purpose of this agreement is to clearly define the terms under which intellectual property has been contributed to the ISAvalidator, ISAconverter or BII Data Manager and thereby allow us to defend the project should there be a legal dispute regarding the software at some future time.

For a list of contributors, please see <http://github.com/ISA-tools/ISAvalidator-ISAconverter-BIImanager/contributors>

## License

The ISAconverter, ISAvalidator & BII Management Tool are licensed under the Mozilla Public License (MPL) version
 1.1/GPL version 2.0/LGPL version 2.1
