# JavaEncFS
A Java Interface for encFS on Linux and Mac


Encrypting files containing personal data has become paramount with the introduction of the GDPR.
A simple tool to achieve this is the open source software encfs, which generates an encrypted file system, and makes it available as an additional mount point or volume.
While it is easy to use on the command line, a graphical user interface might be preferred by many. 
For windows a GUI exists, in the form of [encFS4Win](https://github.com/jetwhiz/encfs4win).
However, for Linux and mac, the available tools are not reliable.
Here are instructions on how to use encFS on the different operating systems. How to install it etc.

## Windows
### Installation
The Windows installation file can be obtained from [encFS4Win](https://github.com/jetwhiz/encfs4win)
Simply double click the file and install the tool (along with all its requirements) using the installer.
### Usage
Once installed, you can start the tool by pressing Start and typing encfs, you will see the encfs programm. Just click it.
A tray icon will be created (A small key icon in the lower right side). 
When you right-click on this icon, You will have options to create new storages, and open existing ones. 
Each Volume will be mapped to a drive letter (e.g. E:) that you can choose. 
While you can have multiple mounts pointing to the same drive letter, they cannot be active simultaneously.

## Unix Systems
### Installation

#### Mac
These instructions are for High Sierra (macOS 10.13), but they should work as well for other macOS versions.
You will need to install the encfs tool and java before you can start. To do so, we will install Homebrew (if it is not yet installed), which makes the installation much easier
Run the following command in a terminal (LaunchPad -> Other/Utilities -> Terminal):

`ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"`

First, we will install java using homebrew:

`brew cask install java`

encfs requires the FUSE (Filesystem in USErspace) package to work, so we will install it as 

`brew cask install osxfuse`

And finally install encfs as:

`brew install encfs`


#### Linux
I assume you are on an apt-based system (our example is for ubuntu 16.04). If you are using a different package manager, you will have to adapt the instructions accordingly.
You will need to install encfs and java.

`sudo apt-get install encfs`

`sudo apt-get install default-jdk`
 
 
After these installations, you can copy the Jar file from [the releases page](https://github.com/sysbiolux/JavaEncFS/releases/tag/1.0) to your machine.
Run the jar file (either by double-click (if your settings are set up to run jar files) or by "right click" -> "open with" -> "(Open)JDK .."


### Usage
After starting the application, you will see an interface (as can be seen in the Docu folder).
You can create new encrypted volumes and mount/unmount volumes using this interface. This is also posible by using the tray icon generated.
Closing the Interface does not close the tool! It will stay open as tray icon, and you can reopen the Interface by right-clicking on the tray icon and select "Open Manager".
Only if you exit from the tray icon, the tool will shut down.

If you do so, all your mounts will be unmounted (disconnected), and you will need to reopen them, if you want to use them again.

#### Password storage
The tool offers a password storage, so that you don't need to remember all passwords for the different storages. If using this feature, please use a strong master password to protect them.
The first time you cick "Add Password", you will be asked to set up a master password. The master password cannot be changed (at the moment). 
If you want to access a volume for which you have saved a password, you will be asked to provide the master key to unlock the stored password. 
A entered master key will be remembered for about 5 minutes, i.e. any other volumes you open in this time which have stored passwords will not require you to enter a password.

