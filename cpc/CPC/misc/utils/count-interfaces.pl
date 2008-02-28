#!/usr/bin/perl

use strict;

$main::basedir = '/home/exp/data/devel/workspace-ecg';
$main::prefix = 'CPC_';
$main::icnt = 0;

parse_dir($main::basedir);

print $main::icnt." interfaces\n";

sub parse_dir
{
	my $d = shift;
	
#	print "DIR: $d\n";
	
	-d $d or die "404: $d\n";
	
	opendir(my $dh, $d);
	while (my $f = readdir($dh))
	{
		if ($f eq '.' || $f eq '..') { next; }
		
		
		my $ff = $d.'/'.$f;
		
		if (-d $ff)
		{
			if ($d eq $main::basedir)
			{
				if ($f !~ /$main::prefix/)
				{
					next;
				}
		 	}
			parse_dir($ff);
		}
		elsif (-f $ff)
		{
			if ($f =~ /^I[A-Z].+\.java$/)
			{
#				my $isif = 0;
#				open(my $fh, '<', $ff) || die "404: $ff\n";
#				while (<$fh>)
#				{
#					if ($_ =~ /interface/) {
#						$isif = 1;
#					} elsif ($_ =~ /class/) {
#						last;
#					}
#				}
#				close($fh);
			
				my $sf = $ff;
				$sf =~ s/^$main::basedir//;
#				print $isif." INTERFACE: $f - $sf\n";
				print "  INTERFACE: $f - $sf\n";
#				if ($isif)
#				{
					$main::icnt++;
#				}
			}
		}
	}
	closedir($dh);
}