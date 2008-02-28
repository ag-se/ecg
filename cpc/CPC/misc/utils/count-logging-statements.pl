#!/usr/bin/perl

use strict;

$main::fcount = 0;
$main::logcount = 0;
$main::assertcount = 0;
$main::loc = 0;

my $dir = '/home/exp/data/devel/workspace-ecg-build';

%main::excluded_dirs = (
	'SddSoc' => 1,
	'.svn' => 1,
	'parsers' => 1,
	'diffmatchpatch' => 1,
	'codereplay' => 1
);

parse_dir($dir);

print "$main::fcount java files, $main::logcount log statements, $main::assertcount assert statements, $main::loc loc\n";

sub parse_dir
{
	my $d = shift;
	
	print "DIR: $d\n";
	
	opendir(my $dh, $d) || die "404: $d\n";
	while (my $f = readdir($dh))
	{
		if ($f eq '.' || $f eq '..' || $f =~ /^\./) { next; }
		
		if ($main::excluded_dirs{$f}) { next; }
	
		my $fd = $d.'/'.$f;
	
		if (-d $fd)
		{
			parse_dir($fd);
		}
		elsif (-f $fd)
		{
			if ($fd =~ /\.java$/)
			{
				$main::fcount++;
				print "JAVA: $fd\n";
				open(my $in, '<', $fd) || die "404: $fd\n";
				
				while (<$in>)
				{
					if ($_ =~ /\s+log(\.|\s*\n)/)
					{
						$main::logcount++;
					}
					if ($_ =~ /assert/)
					{
						$main::assertcount++;
					}
					$main::loc++;
				}
				
				close($in);
			}
		}
	}
}
