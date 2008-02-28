#!/usr/bin/perl

my $dir = '/home/exp/data/devel/workspace-ecg-build';

parse_dir($dir);

sub parse_dir
{
	my $d = shift;
	
	print "DIR: $d\n";
	
	opendir(my $dh, $d) || die "404: $d\n";
	while (my $f = readdir($dh))
	{
		if ($f eq '.' || $f eq '..') { next; }
	
		my $fd = $d.'/'.$f;
	
		if (-d $fd)
		{
			parse_dir($fd);
		}
		elsif (-f $fd)
		{
			if ($fd =~ /\.java$/)
			{
				print "JAVA: $fd\n";
				system("mv -f $fd $fd.bak");
				open(my $in, '<', $fd.'.bak') || die "404: $fd.bak\n";
				open(my $out, '>', $fd) || die "unable to write: $fd\n";
				
				while (<$in>)
				{
					$_ =~ s/\@Override\n//g;
					print $out $_;
				}
				
				close($out);
				close($in);
			}
		}
	}
}
