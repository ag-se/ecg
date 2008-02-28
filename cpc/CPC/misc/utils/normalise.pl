#!/usr/bin/perl

use strict;

foreach my $f (@ARGV)
{
	normalise($f);
}


sub normalise
{
my $f = shift;

if (!-f $f)
{
	die "usage: $0 <file>\n";
}

my %uuids;
my $uuidcnt = 0;

my %oids;
my $oidcnt = 0;

open(FI,'<',$f)||die "404: $f\n";
open(FO,'>',$f.'.out');
while (<FI>)
{
	my $l = $_;
	
	chomp $l;
	$l =~ s/\r//;
	
#	print "IN: $l";
	
	#strip time
	#2007-08-22 14:08:37 [Worker-3]...
	$l =~ s/^\d+\-\d+\-\d+ \d+\:\d+\:\d+ //;
	
	#strip uuids
	#c82afd10-cbd7-4ce1-a29f-af7874585a49
	while ($l =~ /([a-z0-9]{8,8}\-[a-z0-9]{4,4}\-[a-z0-9]{4,4}\-[a-z0-9]{4,4}\-[a-z0-9]{12,12})/g)
	{
		my $uuid = $1;
		my $id = $uuids{$uuid};
		
		if (!$id)
		{
			$id = 'UUID('.++$uuidcnt.')';
			$uuids{$uuid} = $id;
		}
		
#		print "  $uuid => $id\n";
		
		$l =~ s/$uuid/$id/g;
	}
	
	#strip object ids
	#org.hsqldb.jdbc.jdbcConnection@1566b7d
	while ($l =~ /(([a-z0-9]+\.[a-zA-Z0-9\.\;]+)\@([0-9a-f]{3,10}))/g)
	{
		my $all = $1;
		my $class = $2;
		my $oid = $3;

		my $id = $oids{$all};
		
		if (!$id)
		{
			$id = 'OID('.++$oidcnt.')';
			$oids{$all} = $id;
		}
		
#		print "  $all => $id\n";

		$l =~ s/$all/$class\@$id/g;
	}
	
	#CloneFile toString modification data
	#mod: 1187791718000
	$l =~ s/mod\: \d+/mod: x/g;
	
	#Clone toString creation data
	#date: Wed Aug 22 14:08:41 GMT 2007
	$l =~ s/\S{3,3} \S{3,3} \d+ \d+\:\d+\:\d+ \S+ \d+/\(DATE\)/g;
	
	#remove usernames
	$l =~ s/user\: [a-zA-Z]+/user: x/g;
	$l =~ s/creator\: [a-zA-Z]+/creator: x/g;
	
#	print "OUT: $l";
	
	print FO $l."\n";
}
close(FO);
close(FI);

}
