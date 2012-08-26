<?php

$DIR = dirname(__FILE__);
$G_ERROR = 0;
$syndir = "$DIR/new_syntax";
if(!is_dir($syndir))
    exit('no syntax dir');

$files = scandir($syndir);

foreach($files as $file)
{
    if($file == '.' || $file == '..')
        continue;
    
    check_conf($syndir."/$file");
    //break one
    if($G_ERROR == 1)
        break;
}

exit("\nok\n");

function check_conf($file)
{
    if(is_dir($file))
        return;
    $groups = array();
    $linked_groups = array();
    $lines = file($file);
    $save = array();
    foreach($lines as $lnum => $line)
    {
        $line = trim($line);
        $save[$lnum] = $line;
        if(!$line)
            continue;
        if($line == '"' || $line == '#')
        {
            $save[$lnum] = '';
            continue;
        }
        //comment
        if(substr($line, 0, 1) == '"')
        {
            $save[$lnum] = "#".substr($line, 1);
            continue;
        }
        
        if(substr($line, 0, 1) == '#')
        {
            continue;
        }
        
        $exp = preg_split("/\s+/", $line);
        $cmd = array_shift($exp);
        if($cmd == 'syn')
        {
            $cmd2 = array_shift($exp);
            
            if(!in_array($cmd2, array('keyword', 'case', 'region', 'match')))
            {
                e($file, $line, "bad cmd2");
                continue;
            }
            if($cmd2 == 'case')
                continue;
            $groupname = array_shift($exp);
            $kw = implode(' ', $exp);
            if(strpos($kw, 'contained') !== false)
            {
                e($file, $line, "has contained");
                continue;
            }
            $groups[$groupname] = substr($kw, 0, 50);
        }elseif($cmd == 'HiLink'){
            $from = array_shift($exp);
            $to = array_shift($exp);
            
            if(!isset($groups[$from]))
            {
                unset($save[$lnum]);
                //e($file, $line, "$from from group name no exists");
                continue;
            }
            if(!in_array($to, array('Tag','Comment','String','Keyword','Function','AttrName')))
            {
                e($file, $line."  (".$groups[$from].")", "$from to group name no exists");
                continue;
            }
            $linked_groups[$from] = $from;
        }elseif($cmd == 'include'){
            
            e($file, $line, "has include", 0);
            continue;
            
        }else{
            e($file, $line, "bad command");
        }
    }
    $diff = array_diff_key($groups, $linked_groups);
    if($diff)
    {
        $unused = '';
        $cmd = '';
        foreach($diff as $k=>$v)
        {
            $unused .= "$k  ($v)\n";
            $cmd .= "HiLink $k TOGROUP\n";
        }
        e($file, $unused, "group name is unused");
        e($file, $cmd, "++++++++++++++");
    }
    file_put_contents(str_replace('.vim','.conf',$file), implode("\n", $save)); /*"# 920 Text Editor syntax file ==========================================
#
# Modified by: Jecelyin (jecelyin@gmail.com)
#
# Reference from VIM syntax file.
# ======================================================================\n".*/
}

function e($file,  $line, $msg, $error=1)
{
    global $G_ERROR;
    echo "### $msg ($file)
$line

";
    if($error)
        $G_ERROR = $error;
}



